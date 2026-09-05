import { ConversationSummary } from "../lib/types";

interface Props {
  conversations: ConversationSummary[];
  selectedId: string | null;
  onSelect: (id: string) => void;
}

export default function ConversationList({ conversations, selectedId, onSelect }: Props) {
  return (
    <div className="w-72 border-r overflow-y-auto bg-white">
      {conversations.map((c) => (
        <button
          key={c.id}
          onClick={() => onSelect(c.id)}
          className={`w-full text-left px-4 py-3 border-b hover:bg-slate-50 ${
            selectedId === c.id ? "bg-slate-100" : ""
          }`}
        >
          <div className="flex justify-between items-center">
            <span className="font-medium text-slate-800">{c.leadName ?? "Без имени"}</span>
            <span className="text-xs uppercase text-slate-400">{c.channel}</span>
          </div>
          <div className="text-xs text-slate-500">
            {c.ownerType === "BOT" ? "🤖 бот отвечает" : "👤 менеджер ведёт"}
          </div>
        </button>
      ))}
      {conversations.length === 0 && <p className="p-4 text-sm text-slate-400">Пока нет диалогов</p>}
    </div>
  );
}
