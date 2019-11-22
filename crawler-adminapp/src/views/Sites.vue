<template>
  <div>
    <page-title :heading="$t('SITES_TITLE')" :subheading="$t('SITES_SUBTITLE')" :icon="icon" :createNewText="$t('SITES_CREATE_NEW_BUTTON')" @createNew='createNew'></page-title>
    <b-card class="main-card mb-4">
      <b-alert 
        :show="status.message"
        dismissible
        v-model="status.visible"
        :variant="status.type">
            {{ status.message }}
      </b-alert>
      <b-form-group
          :label="$t('SITES_FILTER_TITLE')"
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
              :placeholder="$t('SITES_FILTER_PLACEHOLDER')"
            ></b-form-input>
            <b-input-group-append>
              <b-button :disabled="!filter" @click="filter = ''">{{ $t('SITES_FILTER_CLEAR') }}</b-button>
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
        sort-by="id"
        :sort-desc="false"
        >
        <template v-slot:cell(name)="data">
          <b-button variant="link"
            role="button"
            class="text-info"
            @click="showDetails(data.item)"
          >
            {{ data.value }}
          </b-button>
        </template>
      </b-table>
    </b-card>
    <site-details :v-if="detailsVisible" :site="selectedSite" :action="dialogAction" @sites-updated="onSitesUpdated" />
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
  data() { 
    return {
      dialogAction : 'view',
      selectedSite : {},
      detailsVisible: false,
      icon: 'pe-7s-browser icon-gradient bg-happy-itmeo',
      filter: '',
      filterFields: [ 'name', 'description', 'url' ],
      sites: [],
      status : {}
    };
  },
  computed: {
    displayFields : function() {

    return [
          {
            key: 'id',
            label: this.$t('SITES_TABLE_ID'),
            sortable: true
          },
          {
            key: 'name',
            label: this.$t('SITES_TABLE_NAME'),
            sortable: true
          },
          {
            key: 'description',
            label: this.$t('SITES_TABLE_DESCRIPTION'),
            sortable: false
          },
          {
            key: 'url',
            label: this.$t('SITES_TABLE_URL'),
            sortable: false
          },
          {
            key: 'created_at',
            label: this.$t('SITES_TABLE_CREATED'),
            sortable: true,
            class: 'date-column'
          },
          {
            key: 'last_modified_at',
            label: this.$t('SITES_TABLE_LAST_MODIFIED'),
            sortable: true,
            class: 'date-column'
          }
        ];
    }
  },
  mounted() {
    // Fetch sites on page load
    this.fetchSites();
  },
  methods: {
    async fetchSites() {
      try {
        let response = await SiteService.fetchSites();
        this.sites = response.data;
      } catch (err) {
        // Couldn't get site list, display error
        this.setStatus(this.$t('SITES_FETCH_FAILED'), 'danger');
      }
    },
    showDetails(site) {
      // Set up and display site details modal
      this.selectedSite = site;
      this.dialogAction = 'view';
      this.$bvModal.show('site-details');
    },
    createNew() {
      // Set up and display create site modal
      this.selectedSite = {};
      this.dialogAction = 'add';
      this.$bvModal.show('site-details');
    },
    onSitesUpdated(data) {
      // Event handler for when sites are updated, show response message and reload site list
      this.setStatus(data.message, data.type);
      this.fetchSites();
    },
    setStatus(message, type) {
      // Displays an alert message
      this.status = { message : message, type : type, visible : true };
    }
  }
};
</script>

<style>

.date-column { 
  min-width: 150px;
}

</style>