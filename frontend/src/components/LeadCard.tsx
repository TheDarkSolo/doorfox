import { FunnelStage, LeadResponse } from "../lib/types";

interface Props {
  lead: LeadResponse;
  stages: FunnelStage[];
  onMove: (id: string, stage: FunnelStage) => void;
}

export default function LeadCard({ lead, stages, onMove }: Props) {
  return (
    <div className="bg-white rounded shadow-sm p-3 space-y-1">
      <p className="font-medium text-slate-800">{lead.name ?? "Без имени"}</p>
      <p className="text-xs text-slate-500">{lead.phone ?? lead.instagramHandle}</p>
      {lead.budget && <p className="text-xs text-slate-500">Бюджет: {lead.budget}</p>}
      {lead.projectType && <p className="text-xs text-slate-500">{lead.projectType}</p>}
      <select
        className="text-xs border rounded px-1 py-0.5 w-full mt-1"
        value={lead.funnelStage}
        onChange={(e) => onMove(lead.id, e.target.value as FunnelStage)}
      >
        {stages.map((s) => (
          <option key={s} value={s}>
            {s}
          </option>
        ))}
      </select>
    </div>
  );
}
