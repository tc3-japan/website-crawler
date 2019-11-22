<template>
    <div class="app-page-title">
        <div class="page-title-wrapper">
            <div class="page-title-heading">
                <div class="page-title-icon">
                    <i :class="icon"/>
                </div>
                <div>
                    {{heading}}
                    <div
                        class="page-title-subheading">
                        {{subheading}}
                    </div>
                </div>
            </div>
            <div class="page-title-actions">
                <button type="button" class="btn-shadow d-inline-flex align-items-center btn btn-primary" @click="createNew()">
                    <font-awesome-icon class="mr-2" icon="plus"/>
                    {{createNewText}}
                </button>
            </div>
        </div>
    </div>
</template>

<script>
    import {library} from '@fortawesome/fontawesome-svg-core';
    import {
        faStar,
        faPlus
    } from '@fortawesome/free-solid-svg-icons';
    import {FontAwesomeIcon} from '@fortawesome/vue-fontawesome';

    library.add(
        faStar,
        faPlus,
    );

    export default {
        components: {
            'font-awesome-icon': FontAwesomeIcon,
        },
        props: {
            icon: String,
            heading: String,
            subheading: String,
            createNewText: {
                type: String,
                required: false,
                default: 'Create New'
            }
        },
        methods: {
            createNew() {
                this.$emit('createNew');
            }
        }
    };
</script>
