from typing import List, Optional
import requests
from langchain_core.language_models.chat_models import BaseChatModel
from langchain_core.messages import AIMessage
from langchain_core.outputs import ChatResult
from langchain_core.runnables import RunnableConfig
from langchain_core.callbacks import CallbackManagerForLLMRun
from langchain_core.messages.base import BaseMessage
from pydantic import Field
from langchain_core.outputs import ChatGeneration

class DeepSeekChatModel(BaseChatModel):
    api_key: str = Field(..., description="DeepSeek API key")
    model: str = Field(default="deepseek-chat")
    endpoint: str = Field(default="https://api.deepseek.com/chat/completions")

    def _convert_messages(self, messages: List[BaseMessage]) -> List[dict]:
        def convert_role(role):
            if role == "human":
                return "user"
            if role == "ai":
                return "assistant"
            return role  # e.g., system

        return [{"role": convert_role(m.type), "content": m.content} for m in messages]

    def _call_api(self, messages: List[dict]) -> str:
        headers = {
            "Authorization": f"Bearer {self.api_key}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": self.model,
            "messages": messages,
            "stream": False
        }
        response = requests.post(self.endpoint, json=payload, headers=headers)
        response.raise_for_status()
        data = response.json()
        return data["choices"][0]["message"]["content"]

    def _generate(
        self,
        messages: List[BaseMessage],
        stop: Optional[List[str]] = None,
        run_manager: Optional[CallbackManagerForLLMRun] = None,
        config: Optional[RunnableConfig] = None
    ) -> ChatResult:
        raw_messages = self._convert_messages(messages)
        output = self._call_api(raw_messages)
        return ChatResult(generations=[ChatGeneration(message=AIMessage(content=output))])

    @property
    def _llm_type(self) -> str:
        return "deepseek-chat"
