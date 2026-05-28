import json
import os
import socket
import urllib.error
import urllib.request
from typing import Optional


def _log(message: str) -> None:
    print(message)


class OllamaClient:
    """Small urllib-based Ollama client."""

    def __init__(self, base_url: str | None = None, model: str | None = None):
        self.base_url = (base_url or os.getenv("AGENT_OLLAMA_URL") or "http://127.0.0.1:11434").rstrip("/")
        self.model = model or os.getenv("AGENT_OLLAMA_MODEL") or "gemma4:e2b"
        self.chat_url = f"{self.base_url}/api/chat"
        self.last_error_tag: str | None = None
        self.last_error_detail: str | None = None

    def generate(self, prompt: str, temperature: float = 0.7, timeout_seconds: int = 20) -> Optional[str]:
        """Call Ollama chat API and return generated text, or None on failure."""
        self.last_error_tag = None
        self.last_error_detail = None
        _log(f"[ollama] generate model={self.model}")
        _log(f"[ollama] prompt_length={len(prompt)} chars")

        try:
            data = {
                "model": self.model,
                "messages": [{"role": "user", "content": prompt}],
                "stream": False,
                "format": "json",
                "options": {
                    "temperature": temperature,
                    "num_predict": 2000
                }
            }

            req = urllib.request.Request(
                self.chat_url,
                data=json.dumps(data, ensure_ascii=False).encode("utf-8"),
                headers={"Content-Type": "application/json"},
                method="POST",
            )

            _log("[ollama] waiting for response")
            with urllib.request.urlopen(req, timeout=timeout_seconds) as response:
                result = json.loads(response.read().decode("utf-8"))
                _log("[ollama] response ok")
                message = result.get("message", {})
                if isinstance(message, dict):
                    return message.get("content", "")
                return result.get("response", "")

        except urllib.error.HTTPError as e:
            self.last_error_tag = "MODEL_HTTP_ERROR"
            self.last_error_detail = f"{e.code} {e.reason}"
            if e.code == 404:
                self.last_error_tag = "MODEL_NOT_FOUND"
            _log(f"[ollama] http error: {e.code} - {e.reason}")
            return None

        except socket.timeout as e:
            self.last_error_tag = "MODEL_TIMEOUT"
            self.last_error_detail = str(e)
            _log(f"[ollama] timeout after {timeout_seconds}s")
            return None

        except urllib.error.URLError as e:
            reason = getattr(e, "reason", None)
            if isinstance(reason, socket.timeout):
                self.last_error_tag = "MODEL_TIMEOUT"
            else:
                self.last_error_tag = "LLM_UNAVAILABLE"
            self.last_error_detail = str(e)
            _log("[ollama] connection failed")
            _log("   1. Ollama is running")
            _log(f"   2. service url: {self.base_url}")
            _log(f"   error: {e}")
            return None

        except Exception as e:
            self.last_error_tag = "MODEL_ERROR"
            self.last_error_detail = str(e)
            _log(f"[ollama] generate error: {e}")
            return None


class OpenAICompatibleClient:
    """OpenAI 兼容 API 客户端（支持 DeepSeek、通义千问、OpenAI 等）"""

    def __init__(self, api_key: str | None = None, base_url: str | None = None, model: str | None = None):
        self.api_key = api_key or os.getenv("AGENT_OPENAI_API_KEY") or ""
        self.base_url = (base_url or os.getenv("AGENT_OPENAI_BASE_URL") or "https://api.deepseek.com/v1").rstrip("/")
        self.model = model or os.getenv("AGENT_OPENAI_MODEL") or "deepseek-v4-flash"
        self.last_error_tag: str | None = None
        self.last_error_detail: str | None = None

    def generate(self, prompt: str, temperature: float = 0.7, timeout_seconds: int = 60) -> Optional[str]:
        import time as _time
        self.last_error_tag = None
        self.last_error_detail = None
        _log(f"[openai] generate model={self.model} base_url={self.base_url}")
        _log(f"[openai] prompt_length={len(prompt)} chars")

        max_retries = 3
        base_delay = 2
        for attempt in range(max_retries):
            if attempt > 0:
                delay = base_delay ** attempt
                _log(f"[openai] retry attempt {attempt + 1}/{max_retries} after {delay}s")
                _time.sleep(delay)

            try:
                data = {
                    "model": self.model,
                    "messages": [
                        {"role": "user", "content": prompt}
                    ],
                    "temperature": temperature,
                    "max_tokens": 4000,
                    "stream": False,
                }

                req = urllib.request.Request(
                    f"{self.base_url}/chat/completions",
                    data=json.dumps(data, ensure_ascii=False).encode("utf-8"),
                    headers={
                        "Content-Type": "application/json",
                        "Authorization": f"Bearer {self.api_key}",
                    },
                    method="POST",
                )

                _log("[openai] waiting for response")
                with urllib.request.urlopen(req, timeout=timeout_seconds) as response:
                    result = json.loads(response.read().decode("utf-8"))
                    _log("[openai] response ok")
                    choices = result.get("choices", [])
                    if choices:
                        return choices[0].get("message", {}).get("content", "")
                    self.last_error_tag = "MODEL_ERROR"
                    self.last_error_detail = "empty choices"
                    return None

            except urllib.error.HTTPError as e:
                self.last_error_tag = "MODEL_HTTP_ERROR"
                self.last_error_detail = f"{e.code} {e.reason}"
                if e.code == 401:
                    self.last_error_tag = "AUTH_ERROR"
                    self.last_error_detail = "API Key 无效或已过期"
                    _log(f"[openai] auth error: {e.code}")
                    return None
                if e.code == 404:
                    self.last_error_tag = "MODEL_NOT_FOUND"
                    _log(f"[openai] model not found: {e.code}")
                    return None
                if e.code == 429:
                    self.last_error_tag = "RATE_LIMITED"
                    self.last_error_detail = "请求过于频繁，等待后重试"
                    base_delay = 5
                    _log(f"[openai] rate limited (429), attempt {attempt + 1}/{max_retries}")
                    continue
                _log(f"[openai] http error: {e.code} - {e.reason}")
                if attempt < max_retries - 1:
                    base_delay = 3
                    continue
                return None

            except socket.timeout as e:
                self.last_error_tag = "MODEL_TIMEOUT"
                self.last_error_detail = str(e)
                base_delay = 3
                _log(f"[openai] timeout after {timeout_seconds}s, attempt {attempt + 1}/{max_retries}")
                if attempt < max_retries - 1:
                    continue
                return None

            except urllib.error.URLError as e:
                reason = str(e)
                self.last_error_tag = "LLM_UNAVAILABLE"
                self.last_error_detail = reason
                base_delay = 8
                _log(f"[openai] connection failed (attempt {attempt + 1}/{max_retries}): {e}")
                if attempt < max_retries - 1:
                    continue
                return None

            except Exception as e:
                self.last_error_tag = "MODEL_ERROR"
                self.last_error_detail = str(e)
                _log(f"[openai] generate error (attempt {attempt + 1}/{max_retries}): {e}")
                if attempt < max_retries - 1:
                    _time.sleep(2 ** attempt)
                    continue
                return None

        return None
