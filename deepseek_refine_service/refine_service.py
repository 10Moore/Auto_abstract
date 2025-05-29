from flask import Flask, request, jsonify
from langchain.chains.summarize import load_summarize_chain
from langchain.text_splitter import RecursiveCharacterTextSplitter
from deepseek_chat_model import DeepSeekChatModel


app = Flask(__name__)

# 初始化 DeepSeek Chat 模型
llm = DeepSeekChatModel.construct(api_key="sk-5de04e9f947f49c6991f0021779f8e0a")
text_splitter = RecursiveCharacterTextSplitter(chunk_size=3000, chunk_overlap=300)

@app.route('/refine-summary', methods=['POST'])
def refine_summary():
    text = request.json['text']
    docs = text_splitter.create_documents([text])
    chain = load_summarize_chain(llm, chain_type="refine")
    result = chain.run(docs)
    return jsonify(result)

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)
