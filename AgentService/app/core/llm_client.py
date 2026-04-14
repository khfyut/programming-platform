import urllib.request
import urllib.error
import json
from typing import Optional


class OllamaClient:
    """
    Ollama LLM客户端：通过HTTP调用本地Ollama服务
    使用Python内置urllib，无需安装requests
    """

    def __init__(self, base_url: str = "http://localhost:11434", model: str = "gemma4:e2b"):
        self.base_url = base_url
        self.model = model
        self.generate_url = f"{base_url}/api/generate"

    def generate(self, prompt: str, temperature: float = 0.7) -> Optional[str]:
        """
        调用Ollama生成文本

        Args:
            prompt: 提示词
            temperature: 创造性程度 (0-1)

        Returns:
            生成的文本，失败返回None
        """
        print(f"🤖 调用Ollama，模型: {self.model}")
        print(f"📝 Prompt长度: {len(prompt)} 字符")

        try:
            # 准备请求数据
            data = {
                "model": self.model,
                "prompt": prompt,
                "stream": False,
                "options": {
                    "temperature": temperature
                }
            }

            # 创建请求
            req = urllib.request.Request(
                self.generate_url,
                data=json.dumps(data).encode('utf-8'),
                headers={'Content-Type': 'application/json'},
                method='POST'
            )

            # 发送请求并获取响应（10秒超时）
            print("⏳ 等待Ollama响应...")
            with urllib.request.urlopen(req, timeout=10) as response:
                result = json.loads(response.read().decode('utf-8'))
                print("✅ Ollama响应成功")
                return result.get("response", "")

        except urllib.error.URLError as e:
            print("❌ 无法连接到Ollama服务，请确认：")
            print("   1. Ollama已启动")
            print(f"   2. 服务运行在 {self.base_url}")
            print(f"   错误: {e}")
            return None

        except urllib.error.HTTPError as e:
            print(f"❌ Ollama返回错误: {e.code} - {e.reason}")
            return None

        except Exception as e:
            print(f"❌ 调用Ollama出错: {e}")
            return None
