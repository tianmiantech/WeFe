import { lsTagsKey } from '@js/const/consts';

const mutationTypes = {
    UPDATE_TAGSLIST: 'UPDATE_TAGSLIST',
};

const state = {
    tagsList: [],
};

const getters = {
    tagsList: state => state.tagsList,
};

const mutations = {
    [mutationTypes.UPDATE_TAGSLIST](state, { type, data }) {
        const policy = {

            all() {
                window.localStorage.setItem(lsTagsKey, []);
                state.tagsList = [];
            },

            indexes() {
                for (let i = 0; i < state.tagsList.length; i++) {
                    state.tagsList[i].delete = false;
                    for (let j = 0; j < data.length; j++) {
                        const element = data[j];

                        if (i === element) {
                            state.tagsList[i].delete = true;
                        }
                    }
                }

                for (let i = 0; i < state.tagsList.length; i++) {
                    if (state.tagsList[i].delete) {
                        state.tagsList.splice(i, 1);
                        i--;
                    }
                }
            },

            set() {
                window.localStorage.setItem(lsTagsKey, data);
                state.tagsList = data;
            },
        };

        policy[type]();
    },
};

export default {
    state,
    getters,
    mutations,
    mutationTypes,
};
