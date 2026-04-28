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
        self.generate_url = f"{self.base_url}/api/generate"
        self.last_error_tag: str | None = None
        self.last_error_detail: str | None = None

    def generate(self, prompt: str, temperature: float = 0.7, timeout_seconds: int = 20) -> Optional[str]:
        """Call Ollama and return generated text, or None on failure."""
        self.last_error_tag = None
        self.last_error_detail = None
        _log(f"[ollama] generate model={self.model}")
        _log(f"[ollama] prompt_length={len(prompt)} chars")

        try:
            data = {
                "model": self.model,
                "prompt": prompt,
                "stream": False,
                "format": "json",
                "options": {
                    "temperature": temperature,
                    "num_predict": 600
                }
            }

            req = urllib.request.Request(
                self.generate_url,
                data=json.dumps(data, ensure_ascii=False).encode("utf-8"),
                headers={"Content-Type": "application/json"},
                method="POST",
            )

            _log("[ollama] waiting for response")
            with urllib.request.urlopen(req, timeout=timeout_seconds) as response:
                result = json.loads(response.read().decode("utf-8"))
                _log("[ollama] response ok")
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
