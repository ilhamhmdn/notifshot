import axios from 'axios';
import { Campaign, CreateCampaignRequest, PageResponse } from '../types';

const api = axios.create({
  baseURL: 'http://localhost:8080/api/v1',
});

export const campaignApi = {
  getAll: (tenantId?: string, page = 0, size = 10) =>
    api.get<PageResponse<Campaign>>('/campaigns', {
      params: { tenantId, page, size },
    }).then(r => r.data),

  getById: (id: string) =>
    api.get<Campaign>(`/campaigns/${id}`).then(r => r.data),

  create: (data: CreateCampaignRequest, file: File) => {
    const formData = new FormData();
    const blob = new Blob([JSON.stringify(data)], { type: 'application/json' });
    formData.append('campaign', blob);
    formData.append('file', file);
    return api.post<Campaign>('/campaigns', formData).then(r => r.data);
  },

  retryFailures: (id: string) =>
    api.post(`/campaigns/${id}/retry-failures`).then(r => r.data),
};

export const tenantApi = {
  getAll: () =>
    api.get<any[]>('/tenants').then(r => r.data),

  create: (data: { name: string; email: string }) =>
    api.post<any>('/tenants', {
      ...data,
      monthlyCampaignLimit: 100,
      monthlyMessageLimit: 1000000,
      campaignsUsed: 0,
      messagesUsed: 0,
      active: true,
    }).then(r => r.data),
};