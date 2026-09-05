import { MessageResponse, MessageSender } from "../lib/types";

interface Props {
  messages: MessageResponse[];
}

const senderStyles: Record<MessageSender, string> = {
  LEAD: "bg-white self-start",
  BOT: "bg-blue-100 self-end",
  MANAGER: "bg-green-100 self-end",
};

export default function MessageThread({ messages }: Props) {
  return (
    <div className="flex-1 overflow-y-auto p-4 flex flex-col gap-2">
      {messages.map((m) => (
        <div key={m.id} className={`max-w-md px-3 py-2 rounded-lg shadow-sm ${senderStyles[m.sender]}`}>
          <p className="text-sm text-slate-800 whitespace-pre-wrap">{m.content}</p>
          <p className="text-[10px] text-slate-400 mt-1">{new Date(m.createdAt).toLocaleTimeString()}</p>
        </div>
      ))}
    </div>
  );
}
