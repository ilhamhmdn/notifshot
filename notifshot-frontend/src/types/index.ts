export type Channel = 'EMAIL' | 'SMS' | 'PUSH';
export type CampaignStatus = 'SCHEDULED' | 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';

export interface Campaign {
  id: string;
  tenantId: string;
  name: string;
  channel: Channel;
  messageTemplate: string;
  status: CampaignStatus;
  totalRecipients: number;
  sentCount: number;
  failedCount: number;
  skippedCount: number;
  deliveryRate: number;
  scheduledAt: string | null;
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface CreateCampaignRequest {
  tenantId: string;
  name: string;
  channel: Channel;
  messageTemplate: string;
  transactional: boolean;
  scheduledAt?: string;
}