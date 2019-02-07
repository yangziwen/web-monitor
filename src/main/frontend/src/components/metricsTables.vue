<script>

    import { uuid } from './common.vue';

    export const metricsTable = {
        template: `<Table highlight-row ref="table"
                        :id="id"
                        :data="data"
                        :columns="columns"
                        :height="height"
                        :fixColumn="fixColumn"
                        @on-filter-change="handleFilterChange"
                    />`,
        props: {
            id: {
                default: () => `chart-id-${uuid()}`
            },
            data: {
                default: []
            },
            height: {
                default: 400
            },
            fixColumn: false,
        },
        methods: {
            handleFilterChange(column) {
                this.$emit('on-filter-change', column);
            },
            offsetTop() {
                return this.$refs.table.$el.offsetTop;
            },
            width() {
                return this.$(this.$refs.table.$el).width();
            },
            exportCsv(options) {
                this.$refs.table.exportCsv(options);
            }
        },
        computed: {
            columns() {
                return this.fixColumn ? this.fixedColumns : this.expandableColumns;
            }
        },
        data() {
            const unfixedColumns = [{
                key: 'project',
                title: 'project',
                width: 150,
                sortable: true,
            }, {
                key: 'cnt',
                title: 'total',
                width: 100,
                sortable: true
            }, {
                key: 'errorCnt',
                title: 'error',
                width: 100,
                sortable: true
            }, {
                key: 'max',
                title: 'max (ms)',
                width: 110,
                sortable: true
            }, {
                key: 'min',
                title: 'min (ms)',
                width: 110,
                sortable: true
            }, {
                key: 'avg',
                title: 'avg (ms)',
                width: 120,
                sortable: true,
                filters: [{
                    label: '0-0.2s',
                    value: '0-200'
                }, {
                    label: '0.2-0.5s',
                    value: '200-500'
                }, {
                    label: '0.5-1s',
                    value: '500-1000'
                }, {
                    label: '1-2s',
                    value: '1000-2000'
                }, {
                    label: '2-5s',
                    value: '2000-5000'
                }, {
                    label: '5-10s',
                    value: '5000-10000'
                }, {
                    label: '10s+',
                    value: '10000'
                }],
                filterMultiple: true,
                filterMethod (value, row) {
                    const arr = value.split('-');
                    if (row.avg < arr[0]) {
                        return false;
                    }
                    if (arr.length > 1 && row.avg > arr[1]) {
                        return false;
                    }
                    return true;
                }
            }, {
                key: '95percentile',
                title: '95%(ms)',
                width: 120,
                sortable: true,
                sortMethod(v1, v2, type) {
                    const direction = type == 'desc' ? -1 : 1;
                    const n1 = parseInt(v1.split(/\D/)[0]);
                    const n2 = parseInt(v2.split(/\D/)[0]);
                    return direction * (n1 - n2);
                },
            }];
            const actionRender = (h, params) => {
                return h('div', [
                    h('Button', {
                        props: {
                            type: 'text',
                            size: 'small',
                        },
                        style: {
                            'margin-left': '-10px'
                        },
                        on: {
                            click: () => {
                                this.$emit('on-show-request', params);
                            }
                        }
                    }, '请求分布'),
                    h('Button', {
                        props: {
                            type: 'text',
                            size: 'small',
                        },
                        style: {
                            'margin-left': '-10px'
                        },
                        on: {
                            click: () => {
                                this.$emit('on-show-distribution', params);
                            }
                        }
                    }, '耗时分布')
                ]);
            }
            return {
                expandableColumns: [{
                    key: 'urlPattern',
                    title: 'url',
                    sortable: true
                }, ...unfixedColumns, {
                    key: 'action',
                    title: '操作',
                    width: 110,
                    render: actionRender
                }],
                fixedColumns: [{
                    key: 'urlPattern',
                    title: 'url',
                    width: 300,
                    fixed: 'left',
                    sortable: true
                }, ...unfixedColumns, {
                    key: 'action',
                    title: '操作',
                    width: 110,
                    fixed: 'right',
                    render: actionRender
                }]
            }
        }
    };

    export const reportTable = {
        template: '<Table :id="id" :data="data" :columns="columns" height="600"></Table>',
        props: {
            id: {
                default: () => `chart-id-${uuid()}`
            },
            data: {
                default: []
            }
        },
        data() {
            return {
                columns: [{
                    key: 'url',
                    title: 'url',
                    sortable: true
                }, {
                    key: 'cnt',
                    title: '总数',
                    width: 140,
                    sortable: true
                }, {
                    key: 'errorRatio',
                    title: '错误比例 (%)',
                    width: 140,
                    sortable: true,
                    render: (h, params) => {
                        return h('span', (params.row.errorRatio * 100).toFixed(2) + '%');
                    }
                }, {
                    key: 'slowRatio',
                    title: '慢请求比例 (%)',
                    width: 140,
                    sortable: true,
                    render: (h, params) => {
                        return h('span', (params.row.slowRatio * 100).toFixed(2) + '%');
                    }
                }, {
                    key: 'status',
                    title: '状态',
                    width: 140,
                    render: (h, params) => {
                        if (params.row.status == 'SLOW_WARNING') {
                            return h('div', [
                                h('Tag', {
                                    props: {
                                        color: 'yellow',
                                        type: 'border'
                                    },
                                    style: {
                                        'margin-left': '-10px'
                                    }
                                }, '慢请求警告')
                            ]);
                        }
                        else if (params.row.status == 'ERROR_WARNING') {
                            return h('div', [
                                h('Tag', {
                                    props: {
                                        color: 'red',
                                        type: 'border'
                                    },
                                    style: {
                                        'margin-left': '-10px'
                                    }
                                }, '错误警告')
                            ]);
                        }
                    }
                }]
            }
        }
    };

</script>