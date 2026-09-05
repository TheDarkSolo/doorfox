import { useQuery, useQueryClient } from "@tanstack/react-query";
import { api } from "../lib/api";
import { FunnelStage, LeadResponse } from "../lib/types";
import LeadCard from "../components/LeadCard";

const STAGES: FunnelStage[] = ["CONTACTED", "QUALIFIED", "BOOKED", "VISITED", "SOLD"];
const STAGE_LABELS: Record<FunnelStage, string> = {
  CONTACTED: "Обращение",
  QUALIFIED: "Квалифицирован",
  BOOKED: "Записан",
  VISITED: "Пришёл",
  SOLD: "Сделка",
};

export default function Leads() {
  const queryClient = useQueryClient();
  const { data: leads = [] } = useQuery({
    queryKey: ["leads"],
    queryFn: () => api.get<LeadResponse[]>("/leads"),
  });

  const moveStage = async (id: string, funnelStage: FunnelStage) => {
    await api.patch(`/leads/${id}`, { funnelStage });
    queryClient.invalidateQueries({ queryKey: ["leads"] });
  };

  return (
    <div className="h-screen overflow-x-auto p-4 flex gap-4 bg-slate-50">
      {STAGES.map((stage) => (
        <div key={stage} className="w-64 flex-shrink-0">
          <h2 className="font-semibold text-slate-700 mb-2">{STAGE_LABELS[stage]}</h2>
          <div className="space-y-2">
            {leads
              .filter((l) => l.funnelStage === stage)
              .map((lead) => (
                <LeadCard key={lead.id} lead={lead} stages={STAGES} onMove={moveStage} />
              ))}
          </div>
        </div>
      ))}
    </div>
  );
}
