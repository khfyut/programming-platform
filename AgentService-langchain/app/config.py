import os
from dotenv import load_dotenv

load_dotenv()

class LLMConfig:
    def __init__(self):
        self.provider = os.getenv("AGENT_LLM_PROVIDER", "openai")
        self.api_key = os.getenv("AGENT_OPENAI_API_KEY", "sk-d4dad64bf6d74d8389936ad37f1b2713")
        self.base_url = os.getenv(
            "AGENT_OPENAI_BASE_URL",
            "https://api.deepseek.com/v1"
        )
        self.model_name = os.getenv("AGENT_OPENAI_MODEL", "deepseek-v4-pro")

class AppConfig:
    def __init__(self):
        self.llm = LLMConfig()

_config = None

def load_config() -> AppConfig:
    global _config
    if _config is None:
        _config = AppConfig()
    return _config