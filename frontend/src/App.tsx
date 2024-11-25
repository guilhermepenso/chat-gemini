import { useState } from "react";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Send } from "lucide-react";
import { Avatar, AvatarFallback, AvatarImage } from "./components/ui/avatar";
import { ScrollArea } from "@/components/ui/scroll-area"; // Importe o ScrollArea
import axios from "axios";

// Definição do tipo para os elementos do chat
type ChatMessage = {
  sender: "User" | "AI"; // Apenas duas opções de remetente
  message: string; // Mensagem como texto
};

function App() {
  const [userMessage, setUserMessage] = useState<string>(""); // Mensagem do usuário
  const [chatHistory, setChatHistory] = useState<ChatMessage[]>([]); // Histórico do chat com tipo definido

  // Função de envio de mensagem
  const sendMessage = async () => {
    // Verifica se o campo de entrada não está vazio
    if (!userMessage.trim()) {
      console.log("Mensagem vazia, não enviando.");
      return;
    }

    // Atualiza o histórico de mensagens com a mensagem do usuário
    setChatHistory((prevChatHistory) => [
      ...prevChatHistory,
      { sender: "User", message: userMessage },
    ]);

    try {
      // Envia a mensagem para o backend como um JSON
      const response = await axios.post(
        "http://localhost:8080/chat/ask", // Envia o dado para o endpoint
        { message: userMessage }, // Passa a mensagem dentro de um objeto JSON
        {
          headers: {
            "Content-Type": "application/json", // Define o tipo de conteúdo
          },
        }
      );

      // Aqui estamos assumindo que a resposta da API vem como { response: "Texto da AI" }
      const aiResponse = response.data.response;

      // Atualiza o histórico de mensagens com a resposta da AI
      setChatHistory((prevChatHistory) => [
        ...prevChatHistory,
        { sender: "AI", message: aiResponse },
      ]);

      // Limpa o campo de entrada após o envio
      setUserMessage("");
    } catch (error) {
      console.error("Erro ao enviar a mensagem:", error);
    }
  };

  return (
    <div className="flex min-h-screen bg-neutral-800 items-center justify-center">
      <Card className="w-[740px] h-[700px] grid grid-rows-[min-content_1fr_min-content]">
        <CardHeader>
          <CardTitle className="text-neutral-600">Chat AI</CardTitle>
          <CardDescription/>
        </CardHeader>
        <ScrollArea className="h-full w-full">
          <CardContent className="text-white space-y-5 overflow-y-auto">
            {/* Exibe as mensagens do histórico */}
            {chatHistory.map((chat, index) => (
              <div
                key={index}
                className={`flex ${chat.sender === "User" ? "" : "justify-end"} gap-3 text-slate-200 text-sm`}
              >
                {chat.sender === "User" ? (
                  <>
                    <Avatar>
                      <AvatarFallback>User</AvatarFallback>
                      <AvatarImage src="https://static.vecteezy.com/system/resources/previews/023/465/688/non_2x/contact-dark-mode-glyph-ui-icon-address-book-profile-page-user-interface-design-white-silhouette-symbol-on-black-space-solid-pictogram-for-web-mobile-isolated-illustration-vector.jpg" />
                    </Avatar>
                    <div className="leading-relaxed rounded-xl p-1">
                      <span className="block font-bold text-slate-400">Usuário</span>
                      <div className="bg-slate-600 p-1 rounded-xl pr-2">
                        <span>{chat.message}</span>
                      </div>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="leading-relaxed rounded-xl p-1">
                      <span className="flex justify-end font-bold text-slate-400">AI</span>
                      <div className="bg-slate-600 p-1 rounded-xl pr-2">
                        <span>{chat.message}</span>
                      </div>
                    </div>
                    <Avatar>
                      <AvatarFallback>AI</AvatarFallback>
                      <AvatarImage src="https://icons.veryicon.com/png/o/education-technology/blue-gray-solid-blend-icon/artificial-intelligence-5.png" />
                    </Avatar>
                  </>
                )}
              </div>
            ))}
          </CardContent>
        </ScrollArea>
        <CardFooter className="space-x-2">
          {/* Campo de entrada da mensagem do usuário */}
          <Input
            value={userMessage} // Ligação com o estado `userMessage`
            onChange={(e) => setUserMessage(e.target.value)} // Atualiza o estado a cada digitação
            placeholder="Me pergunte algo..."
            className="text-white"
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                sendMessage(); // Envia a mensagem ao pressionar "Enter"
              }
            }}
          />

          {/* Botão de envio */}
          <Button onClick={sendMessage} className="text-black bg-white">
            <Send />
          </Button>
        </CardFooter>
      </Card>
    </div>
  );
}

export default App;
