<template>
  <div>
    <page-title :heading="heading" :subheading="subheading" :icon="icon" createNewText="Create New Site" @createNew='createNew'></page-title>
    <b-card class="main-card mb-4">
      <b-form-group
          label="Filter"
          label-cols-sm="7"
          label-align-sm="right"
          label-size="sm"
          label-for="filterInput"
          class="mb-0"
        >
          <b-input-group size="sm" class="mb-3">
            <b-form-input
              v-model="filter"
              type="search"
              id="filterInput"
              placeholder="Type to Search"
            ></b-form-input>
            <b-input-group-append>
              <b-button :disabled="!filter" @click="filter = ''">Clear</b-button>
            </b-input-group-append>
          </b-input-group>
        </b-form-group>
      
      <b-table 
        striped="striped" 
        hover="hover" 
        :items="sites" 
        :fields="displayFields"
        :filter="filter"
        :filterIncludedFields="filterFields"
        >
        <template v-slot:cell(name)="data">
          <b-button variant="link"
            role="button"
            class="text-info"
            @click="showDetails(sites[data.index])"
          >
            {{ data.value }}
          </b-button>
        </template>
      </b-table>
    </b-card>
    <site-details :v-if="detailsVisible" :site="selectedSite" :action="dialogAction" />
  </div>
</template>

<script>
import PageTitle from '../components/PageTitle.vue';
import SiteDetails from '../components/SiteDetails.vue';
import SiteService from '@/services/SiteService';

export default {
  components: {
    PageTitle,
    SiteDetails
  },
  data: () => ({
    heading: 'Sites',
    dialogAction : 'view',
    subheading: '',
    selectedSite : {},
    detailsVisible: false,
    icon: 'pe-7s-browser icon-gradient bg-happy-itmeo',
    displayFields: ['id', 'name', 'description', 'url', 'createdAt', 'lastModifiedAt'],
    filter: '',
    filterFields: [ 'name', 'description', 'url' ],
    sites: []
  }),
  mounted() {
    this.fetchSites();
  },
  methods: {
    async fetchSites() {
      try {
        let response = await SiteService.fetchSites();
        console.log('response', response);
        this.sites = response.data.sites;
      } catch (err) {
        // TODO: display error
        console.log('Could not fetch sites', err);
      }
    },
    showDetails(site) {
      console.log('Showing data for id ' + site);

      this.selectedSite = site;
      this.dialogAction = 'view';
      this.$bvModal.show('site-details');
    },
    createNew() {
      this.selectedSite = {};
      this.dialogAction = 'add';
      this.$bvModal.show('site-details');
    }
  }
};
</script>