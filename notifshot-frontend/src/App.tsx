import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Layout } from './components/layout/Layout';
import { CampaignList } from './pages/CampaignList';
import { CampaignCreate } from './pages/CampaignCreate';
import { CampaignDetail } from './pages/CampaignDetail';

function App() {
  return (
    <BrowserRouter>
      <Layout>
        <Routes>
          <Route path="/" element={<CampaignList />} />
          <Route path="/create" element={<CampaignCreate />} />
          <Route path="/campaigns/:id" element={<CampaignDetail />} />
        </Routes>
      </Layout>
    </BrowserRouter>
  );
}

export default App;