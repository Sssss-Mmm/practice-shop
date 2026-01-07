import axios from "axios";

const API_URL = "/api/chatbot";

const chat = (message) => {
  return axios.post(API_URL + "/chat", { message });
};

const ChatbotService = {
  chat,
};

export default ChatbotService;
