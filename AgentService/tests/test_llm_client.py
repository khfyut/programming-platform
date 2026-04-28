import io
import json
import urllib.error
from unittest import TestCase
from unittest.mock import patch

from app.core.llm_client import OllamaClient


class FakeResponse:
    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, traceback):
        return False

    def read(self):
        return json.dumps({"response": "ok"}).encode("utf-8")


class OllamaClientLoggingTest(TestCase):
    def test_generate_does_not_crash_when_stdout_cannot_encode_status_icons(self):
        gbk_stdout = io.TextIOWrapper(io.BytesIO(), encoding="gbk", errors="strict")

        with patch("sys.stdout", gbk_stdout), patch("urllib.request.urlopen", return_value=FakeResponse()):
            result = OllamaClient().generate("hello", timeout_seconds=1)

        self.assertEqual("ok", result)

    def test_generate_uses_json_mode_and_bounded_output(self):
        captured = {}

        def fake_urlopen(request, timeout):
            captured["payload"] = json.loads(request.data.decode("utf-8"))
            return FakeResponse()

        with patch("urllib.request.urlopen", side_effect=fake_urlopen):
            result = OllamaClient().generate("hello", timeout_seconds=1)

        self.assertEqual("ok", result)
        self.assertEqual("json", captured["payload"]["format"])
        self.assertLessEqual(captured["payload"]["options"]["num_predict"], 700)

    def test_generate_marks_ollama_connection_failure_as_unavailable(self):
        client = OllamaClient(base_url="http://127.0.0.1:11434", model="missing")

        with patch("urllib.request.urlopen", side_effect=urllib.error.URLError("connection refused")):
            result = client.generate("hello", timeout_seconds=1)

        self.assertIsNone(result)
        self.assertEqual("LLM_UNAVAILABLE", client.last_error_tag)
