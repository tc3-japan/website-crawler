import Api from './api';

function fetchSites() {
    return Api.get('/manufacturers/sites');
};

function fetchSiteDetails(id) {
    return Api.get(`/manufacturers/sites/${id}`);
};

function createNewSite(body) {
    return Api.post(`/manufacturers/sites`);
};

function editSite(id, body) {
    return Api.put(`/manufacturers/sites/${id}`, body);
};

function deleteSite(id) {
    return Api.delete(`/manufacturers/sites/${id}`);
};

export default {
    fetchSites,
    fetchSiteDetails,
    createNewSite,
    editSite,
    deleteSite
};