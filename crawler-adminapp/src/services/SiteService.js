import { api } from './api';

function fetchSites() {
    return api().get('/websites', {}, { 
        auth: {
          username: 'admin',
          password: 'adminpass'
        } });
}

function fetchSiteDetails(id) {
    return api().get(`/websites/${id}`);
}

function createNewSite(body) {
    return api().post(`/websites`, body);
}

function updateSite(body) {
    let id = body.id;
    delete body.id;
    return api().put(`/websites/${id}`, body);
}

function deleteSite(id) {
    return api().delete(`/websites/${id}`);
}

export default {
    fetchSites,
    fetchSiteDetails,
    createNewSite,
    updateSite,
    deleteSite
};