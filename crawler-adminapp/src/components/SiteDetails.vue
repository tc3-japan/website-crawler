<template>
  <b-modal id="site-details" ref="modal" :title="title" size="xl" scrollable @show="show" @close="close" @cancel="close" @hidden="hidden">
    <div class="card-body">
      <b-alert 
        :show="status.message"
        dismissible
        v-model="status.visible"
        :variant="status.type">
            {{ status.message }}
      </b-alert>
      <form class>
        <div class="position-relative row form-group">
          <label for="name" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_NAME') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="name"
              id="name"
              v-model="siteDetails.name"
              :placeholder="$t('SITE_DETAILS_NAME_PLACEHOLDER')"
              type="text"
              v-bind:class="{ 'form-control': true, 'is-invalid' : $v.siteDetails.name.$error}"
            />
            <div v-if="$v.siteDetails.$error">
              <div v-if="!$v.siteDetails.name.required" class="error invalid-feedback">{{$t('SITE_VALIDATION_NAME_REQUIRED')}}</div>
            </div>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="description" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_DESCRIPTION') }}</label>
          <div class="col-sm-10">
            <textarea 
              :readonly="readOnly" 
              name="description" 
              id="description" 
              v-model="siteDetails.description" 
              class="form-control"
              :placeholder="$t('SITE_DETAILS_DESCRIPTION_PLACEHOLDER')"
            ></textarea>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="url" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_URL') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="url"
              v-model="siteDetails.url"
              :placeholder="$t('SITE_DETAILS_URL_PLACEHOLDER')"
              type="text"
              v-bind:class="{ 'form-control': true, 'is-invalid' : $v.siteDetails.url.$error}"
            />
            <div v-if="$v.siteDetails.$error">
              <div v-if="!$v.siteDetails.url.required" class="error invalid-feedback">{{$t('SITE_VALIDATION_URL_REQUIRED')}}</div>
            </div>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="content_url_patterns" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_URL_PATTERN') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="content_url_patterns"
              id="content_url_patterns"
              v-model="siteDetails.content_url_patterns"
              :placeholder="$t('SITE_DETAILS_URL_PATTERN_PLACEHOLDER')"
              type="text"
              v-bind:class="{ 'form-control': true, 'is-invalid' : $v.siteDetails.content_url_patterns.$error}"
            />
            <div v-if="$v.siteDetails.$error">
              <div v-if="!$v.siteDetails.content_url_patterns.required" class="error invalid-feedback">{{$t('SITE_VALIDATION_URL_PATTERNS_REQUIRED')}}</div>
            </div>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="category_extraction_pattern" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_CATEGORY_PATTERN') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="category_extraction_pattern"
              id="category_extraction_pattern"
              v-model="siteDetails.category_extraction_pattern"
              :placeholder="$t('SITE_DETAILS_CATEGORY_PATTERN_PLACEHOLDER')"
              type="text"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="content_selector" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_SELECTORS') }}</label>
          <div class="col-sm-10">
            <textarea 
              :readonly="readOnly" 
              name="content_selector" 
              id="content_selector" 
              v-model="siteDetails.content_selector" 
              class="form-control"
              :placeholder="$t('SITE_DETAILS_SELECTORS_PLACEHOLDER')"
            ></textarea>
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="supports_robots_txt" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_ROBOTS') }}</label>
          <div class="col-sm-10">
            <toggle-button 
              :disabled="readOnly"
              name="supports_robots_txt"
              id="supports_robots_txt"
              :width="65"
              :height="35"
              v-model="siteDetails.supports_robots_txt"
              :labels="true"
              :color="{checked: '#28a745', unchecked: '#434055'}"
              :font-size="14"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="crawl_max_depth" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_DEPTH') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="crawl_max_depth"
              id="crawl_max_depth"
              v-model="siteDetails.crawl_max_depth"
              :placeholder="$t('SITE_DETAILS_DEPTH_PLACEHOLDER')"
              type="number"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="crawl_time_limit" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_MAX_TIME') }}</label>
          <div class="col-sm-10">
            <input
              :readonly="readOnly"
              name="crawl_time_limit"
              id="crawl_time_limit"
              v-model="siteDetails.crawl_time_limit"
              :placeholder="$t('SITE_DETAILS_MAX_TIME_PLACEHOLDER')"
              type="number"
              class="form-control"
            />
          </div>
        </div>
        <div class="position-relative row form-group">
          <label for="crawl_interval" class="col-sm-2 col-form-label">{{ $t('SITE_DETAILS_INTERVAL') }}</label>
          <div class="col-sm-10">
            <input
              :readonly='readOnly'
              name="crawl_interval"
              id="crawl_interval"
              v-model="siteDetails.crawl_interval"
              :placeholder="$t('SITE_DETAILS_INTERVAL_PLACEHOLDER')"
              type="number"
              class="form-control"
            />
          </div>
        </div>
      </form>
    </div>

    <template v-slot:modal-header="{ cancel }" >
      <h5 class="modal-title">{{ title }}</h5>
      <div>
        <b-button v-show="currentAction!=='add'" variant="danger" size="lg" class="icon-button" @click="deleteSite"><i class="pe-7s-trash"/></b-button>
        <button type="button" aria-label="Close" @click="cancel" class="close">×</button>
      </div>
    </template>

    <template v-slot:modal-footer="{ ok, cancel }">
      <!-- Emulate built in modal footer ok and cancel button actions -->
      <b-button v-show="currentAction==='add'" variant="success" @click="createNew()">{{ $t('CREATE_BUTTON') }}</b-button>
      <b-button v-show="currentAction==='edit'" variant="success" @click="update()">{{ $t('SAVE_BUTTON') }}</b-button>
      <b-button v-show="currentAction==='view'" variant="success" @click="edit()">{{ $t('EDIT_BUTTON') }}</b-button>
      <b-button v-show="readOnly" variant="danger" @click="cancel()">{{ $t('CLOSE_BUTTON') }}</b-button>
      <b-button v-show="!readOnly" variant="danger" @click="cancel()">{{ $t('CANCEL_BUTTON') }}</b-button>
    </template>
  </b-modal>
</template>

<script>
import SiteService from '@/services/SiteService';
import { required } from 'vuelidate/lib/validators';

export default {
  name: 'SiteDetails',
  props: {
    site: {
      type: Object,
      required: false
    },
    action: {
      type: String,
      required: false
    }
  },
  watch: {
    action: function (current, previous) {
      this.currentAction = current;
    },
    site: function (current, previous) {
      this.siteDetails = JSON.parse(JSON.stringify(current));
    },
    siteDetails : {
      handler(current, previous)
      {
        if (this.initialValue)
          this.initialValue = false;
        else 
          this.dirty = true;
      },
      deep: true
    }
  },
  data() {
    return {
      siteDetails: JSON.parse(JSON.stringify(this.site)),
      currentAction: this.action,
      status : {},
      dirty : false,
      initialValue : false
    };
  },
  computed: {
    title() {
      console.log('GENERATING TITLE ', this.currentAction);
      if (this.currentAction === 'add') return this.$t('SITE_DETAILS_ADD');
      else if (this.currentAction === 'edit') return this.$t('SITE_DETAILS_EDIT');
      else return this.siteDetails.name;
    },
    readOnly() {
      return this.currentAction === 'view';
    }
  },
  methods: {
    edit() {
      this.currentAction = 'edit';
    },
    close(bvModalEvt) {
      if ((this.currentAction === 'edit' || this.currentAction === 'add') && this.dirty) {
        bvModalEvt.preventDefault();
        this.$bvModal.msgBoxConfirm(this.$t('SITE_CANCEL_UPDATE_CONFIRMATION'), 
        { 
          title : this.$t('SITE_CANCEL_UPDATE_CONFIRMATION_TITLE'),
          okTitle: this.$t('YES_BUTTON'),
          cancelTitle: this.$t('NO_BUTTON'),
        })
        .then((confirmationResponse) => {
          if (confirmationResponse === true) {
            this.$emit('Close');
            this.$refs.modal.hide();
            this.$v.$reset();
            this.dirty = false;
          }
        });
      }
      else {
        this.$emit('Close');
        this.$v.$reset();
      }
    },
    hidden() {
      this.status = {};
    },
    show() {
      console.log('showing', this.siteDetails);
      console.log('this.action', this.action);
      // this.site = this.siteDetails;
      // this.currentAction = this.action;
      this.siteDetails = JSON.parse(JSON.stringify(this.site));
      this.currentAction = this.action;
      this.dirty = false;
      this.initialValue = true;
    },
    createNew() {
      this.$v.siteDetails.$touch();
      if(this.$v.siteDetails.$error)
        return;

      console.log('creating a new site:', this.siteDetails);
      SiteService.createNewSite(this.siteDetails)
      .then(respone => {
          this.$emit('sites-updated', { message : this.$t('SITE_CREATE_SUCCESS'), type : 'success' });
          this.$bvModal.hide('site-details');
      })
      .catch(err => {
          console.log('failed to create');
          this.displayErrorResponse(err);
      });
    },
    update() {
      this.$v.siteDetails.$touch();
      if(this.$v.siteDetails.$error)
        return;

      console.log('Updating existing site:', this.siteDetails);
      SiteService.updateSite(this.siteDetails)
      .then(respone => {
          this.$emit('sites-updated', { message : this.$t('SITE_UPDATE_SUCCESS'), type : 'success' });
          this.$bvModal.hide('site-details');
      })
      .catch(err => {
        this.displayErrorResponse(err);
        console.log('failed to update');
      });
    },
    deleteSite() {
      console.log('Deleting site:', this.siteDetails.id);

      this.$bvModal.msgBoxConfirm(this.$t('SITE_DELETE_CONFIRMATION', { filename : this.siteDetails.name }), 
      { 
        title : this.$t('SITE_DELETE_CONFIRMATION_TITLE'),
        okTitle: this.$t('YES_BUTTON'),
        cancelTitle: this.$t('NO_BUTTON'),
      })
      .then((confirmationResponse) => {

        if (confirmationResponse === false)
          return;

        SiteService.deleteSite(this.siteDetails.id)
        .then(response => {
          this.$emit('sites-updated', { message : this.$t('SITE_DELETE_SUCCESS'), type : 'success' });
          this.$bvModal.hide('site-details');
          console.log('deleted');
        })
        .catch(err => {
          console.log('failed to update');
          this.displayErrorResponse(err);
        });
      });
    },
    setStatus(message, type) {
      this.status = { message : message, type : type, visible : true };
    },
    displayErrorResponse(error, fallbackMessageKey) {
      console.log('error.response', error.response);
      let messageKey = (error.response && error.response.data && error.response.data.message) ? error.response.data.message : fallbackMessageKey;
      let message = this.$t(messageKey || 'CRUD_GENERIC_FAILURE');
      this.setStatus(message, 'danger');
    }
  },
  validations: {
    siteDetails :{
      name: {
        required
      },
      url: {
        required
      },
      content_url_patterns : {
        required
      }
    }
}
};
</script>

<style scoped>

.icon-button {
  padding: 2px 8px;
}

.invalid-feedback {
  display: block;
  font-size: 1.0em;
}

</style>