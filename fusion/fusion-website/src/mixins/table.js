/**
 * 表格mixin
 */

export default (function () {
    return {
        name: 'mixin-table',
        data () {
            return {
                loading:    true,
                getListApi: '',
                list:       [],
                datePicker: [],
                search:     {},
                pagination: {
                    page_index: 1,
                    page_size:  10,
                    total:      0,
                },
            };
        },
        watch: {
            '$route.query': {
                handler (val) {
                    this._getUrlParams(val);
                },
                deep: true,
            },
        },
        methods: {
            _getUrlParams (queries) {
                for (const key in queries) {
                    this.search[key] = queries[key];
                }
                this.pagination.page_index = +(queries.page_index || 1);
                this.pagination.page_size = +(queries.page_size || 10);

                this.getList();
            },
            async getList (to) {
                this.loading = true;
                this.list = [];
                const { code, data } = await this.$http.get(this.getListApi, {
                    params: {
                        ...this.search,
                        page_index: this.pagination.page_index - 1,
                        page_size:  this.pagination.page_size,
                    },
                    headers: this.headers || {},
                });

                if (code === 0) {
                    this.list = data.list;
                    this.pagination.total = data.total;

                    if (to) {
                        this.$router.push({
                            query: {
                                ...this.search,
                                page_index: this.pagination.page_index,
                                page_size:  this.pagination.page_size,
                            },
                        });
                    }
                }
                this.loading = false;
            },
            currentPageChange (val) {
                const { query } = this.$route;

                this.$router.push({
                    query: {
                        ...query,
                        page_index: val,
                    },
                });
            },
            pageSizeChange (val) {
                const { query } = this.$route;

                this.$router.push({
                    query: {
                        ...query,
                        page_size: val,
                    },
                });
            },
        },
    };
})();
