"use client";

import { ListChecks } from "lucide-react";
import { PageHeader, EmptyState } from "@/components/ui/PageHeader";
import { Card } from "@/components/ui/Card";

export default function TasksPage() {
  return (
    <div>
      <PageHeader icon={ListChecks} title="Tasks" description="Your assigned work." />
      <Card className="mt-6">
        <EmptyState icon={ListChecks} message="Task integration is coming soon." />
      </Card>
    </div>
  );
}
