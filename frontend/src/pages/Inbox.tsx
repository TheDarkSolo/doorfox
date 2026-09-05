import { useEffect, useMemo, useState } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";
import { useAuth } from "../lib/auth";
import { createStompClient } from "../lib/ws";
import { ConversationSummary, MessageResponse } from "../lib/types";
import ConversationList from "../components/ConversationList";
import MessageThread from "../components/MessageThread";

export default function Inbox() {
  const { token, logout } = useAuth();
  const queryClient = useQueryClient();
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [draft, setDraft] = useState("");

  const { data: conversations = [] } = useQuery({
    queryKey: ["conversations"],
    queryFn: () => api.get<ConversationSummary[]>("/conversations"),
  });

  const { data: messages = [] } = useQuery({
    queryKey: ["messages", selectedId],
    queryFn: () => api.get<MessageResponse[]>(`/conversations/${selectedId}/messages`),
    enabled: !!selectedId,
  });

  const selected = useMemo(
    () => conversations.find((c) => c.id === selectedId) ?? null,
    [conversations, selectedId],
  );

  // Live-updates the conversation list (new leads, ownership changes).
  useEffect(() => {
    if (!token) return;
    const client = createStompClient(token);
    client.onConnect = () => {
      client.subscribe("/topic/inbox", (frame) => {
        const updated: ConversationSummary[] = JSON.parse(frame.body);
        queryClient.setQueryData<ConversationSummary[]>(["conversations"], (prev = []) => {
          const byId = new Map(prev.map((c) => [c.id, c]));
          for (const u of updated) byId.set(u.id, u);
          return Array.from(byId.values()).sort(
            (a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime(),
          );
        });
      });
    };
    client.activate();
    return () => {
      client.deactivate();
    };
  }, [token, queryClient]);

  // Live-appends new messages to the open thread.
  useEffect(() => {
    if (!token || !selectedId) return;
    const client = createStompClient(token);
    client.onConnect = () => {
      client.subscribe(`/topic/conversations/${selectedId}`, (frame) => {
        const message: MessageResponse = JSON.parse(frame.body);
        queryClient.setQueryData<MessageResponse[]>(["messages", selectedId], (prev = []) => [...prev, message]);
      });
    };
    client.activate();
    return () => {
      client.deactivate();
    };
  }, [token, selectedId, queryClient]);

  const takeover = async () => {
    if (!selectedId) return;
    await api.post(`/conversations/${selectedId}/takeover`);
    queryClient.invalidateQueries({ queryKey: ["conversations"] });
  };

  const release = async () => {
    if (!selectedId) return;
    await api.post(`/conversations/${selectedId}/release`);
    queryClient.invalidateQueries({ queryKey: ["conversations"] });
  };

  const send = async () => {
    if (!selectedId || !draft.trim()) return;
    await api.post(`/conversations/${selectedId}/messages`, { text: draft });
    setDraft("");
  };

  return (
    <div className="h-screen flex flex-col">
      <header className="flex justify-between items-center px-4 py-2 border-b bg-white">
        <h1 className="font-semibold text-slate-800">Inbox</h1>
        <button onClick={logout} className="text-sm text-slate-500">
          Выйти
        </button>
      </header>
      <div className="flex flex-1 overflow-hidden">
        <ConversationList conversations={conversations} selectedId={selectedId} onSelect={setSelectedId} />
        <div className="flex-1 flex flex-col">
          {selected ? (
            <>
              <div className="flex justify-between items-center px-4 py-2 border-b bg-white">
                <span className="text-sm text-slate-600">
                  {selected.ownerType === "BOT" ? "Бот отвечает автоматически" : "Диалог у менеджера"}
                </span>
                {selected.ownerType === "BOT" ? (
                  <button onClick={takeover} className="text-sm bg-slate-800 text-white px-3 py-1 rounded">
                    Перехватить
                  </button>
                ) : (
                  <button onClick={release} className="text-sm bg-slate-200 px-3 py-1 rounded">
                    Вернуть боту
                  </button>
                )}
              </div>
              <MessageThread messages={messages} />
              {selected.ownerType === "MANAGER" && (
                <div className="p-3 border-t bg-white flex gap-2">
                  <input
                    className="flex-1 border rounded px-3 py-2"
                    value={draft}
                    onChange={(e) => setDraft(e.target.value)}
                    onKeyDown={(e) => e.key === "Enter" && send()}
                    placeholder="Написать клиенту..."
                  />
                  <button onClick={send} className="bg-slate-800 text-white px-4 rounded">
                    Отправить
                  </button>
                </div>
              )}
            </>
          ) : (
            <div className="flex-1 flex items-center justify-center text-slate-400">Выберите диалог слева</div>
          )}
        </div>
      </div>
    </div>
  );
}
