/**
 * table mixins
 */

export default (function () {
    return {
        name: 'mixin-table',
        data () {
            return {
                loading:       true,
                // $parent:    {},
                getListApi:    '',
                requestMethod: 'get',
                list:          [],
                datePicker:    [],
                search:        {},
                pagination:    {
                    page_index: 1,
                    page_size:  20,
                    total:      0,
                },
                defaultSearch: false, // default calling
                fillUrlQuery:  true, // fill form from url query
                watchRoute:    true, // watch the $route //! over 1 table in one page, must set to false, and let parent component watch the $route
                turnPageRoute: true, // turn pages update route path
                unUseParams:   [], // ignore url query, will not fill form
            };
        },
        watch: {
            $route: {
                handler (to, from) {
                    if (to.name === from.name) {
                        if (this.fillUrlQuery) {
                            this._getUrlParams();
                        }
                        if (this.watchRoute) {
                            this.getList();
                        }
                    }
                },
                deep: true,
            },
        },
        created () {
            if (this.fillUrlQuery) {
                this._getUrlParams();
            }
            if (this.defaultSearch) {
                this.getList();
            }
        },
        methods: {
            _getUrlParams () {
                const { query } = this.$route;

                for (const $key in this.search) {
                    this.search[$key] = '';
                }
                if(this.unUseParams) {
                    for (const key in query) {
                        if (!this.unUseParams.includes(key)) {
                            this.search[key] = query[key] || '';
                        }
                    }
                }
                this.pagination.page_index = +(query.page_index || 1);
                this.pagination.page_size = +(query.page_size || this.pagination.page_size);
            },

            async getList (opt = {
                to:              false, // call router.push automatically
                resetSearch:     false, // reset search fields
                resetPagination: false, // reset pagination fields
            }) {
                if (this.vData) {
                    for (const key in this.vData) {
                        const val = this.vData[key];

                        this[key] = val;
                    }
                    this.getListApi = this.vData.getListApi;
                }
                if (!this.getListApi) return;

                this.loading = true;

                // reset search fields
                if (opt.resetSearch) {
                    for (const key in this.search) {
                        this.search[key] = '';
                    }
                    this.pagination.total = 0;
                    this.pagination.page_index = 1;
                }
                // reset pagination fields
                if (opt.resetPagination) {
                    this.pagination.total = 0;
                    this.pagination.page_index = 1;
                }

                // call router.push automatically
                if (opt.to) {
                    const { watchRoute } = this;

                    if (watchRoute) {
                        this.watchRoute = false;
                    }
                    this.$router.push({
                        query: {
                            ...this.search,
                            page_index: this.pagination.page_index,
                            page_size:  this.pagination.page_size,
                        },
                    });
                    if (watchRoute) {
                        this.watchRoute = true;
                    }
                }
                // watch $route
                if (this.watchRoute) {
                    // update pagination
                    const { query } = this.$route;

                    this.pagination.page_index = +(query.page_index || 1);
                    this.pagination.page_size = +(query.page_size || this.pagination.page_size);
                }
                this.unUseParams && this.unUseParams.forEach(key => {
                    this.search[key] = '';
                });
                const params = {
                    [this.requestMethod === 'get' ? 'params' : 'data']: Object.assign(this.search, {
                        page_index: this.pagination.page_index - 1,
                        page_size:  this.pagination.page_size || 20,
                    }),
                };

                const { code, data } = await this.$http[this.requestMethod](this.getListApi, params);

                if (code === 0) {
                    this.list = data.list || [];
                    if (this.vData) {
                        this.vData.list = data.list || [];
                    }
                    this.pagination.total = data.total;
                    this.afterTableRender(data.list);
                }

                setTimeout(() => {
                    this.loading = false;
                    if (this.vData) {
                        this.vData.loading = false;
                    }
                }, 100);
            },

            afterTableRender () { },

            currentPageChange (val) {
                if (this.watchRoute || this.turnPageRoute) {
                    this.$router.push({
                        query: {
                            ...this.search,
                            page_index: val,
                        },
                    });
                } else {
                    this.pagination.page_index = val;
                    this.getList();
                }
            },

            pageSizeChange (val) {
                if (this.watchRoute || this.turnPageRoute) {
                    this.$router.push({
                        query: {
                            ...this.search,
                            page_size: val,
                        },
                    });
                } else {
                    this.pagination.page_size = val;
                    this.getList();
                }
            },
        },
    };
})();
