<style scoped>
   .vertical-center-modal {
        display: flex;
        align-items: center;
        justify-content: center;
        .ivu-modal{
            top: 0;
        }
    }
   div.charts > div {
       float: left;
       margin-bottom: 20px;
   }
   #date-picker-wrapper {
       margin-bottom: 10px;
   }
</style>
<template>
<div class="metrics-area">
    <div id="date-picker-wrapper">
        <default-date-picker :initRange="dateRange"
            @on-change="changeDateRange"></default-date-picker>
        </Date-picker>
    </div>
    <div>
        <metrics-table :data="tableData" @on-show-distribution="renderDistributionChart"></metrics-table>
        <Modal v-model="showDistributionChart" width="640"
                class-name="vertical-center-modal">
            <p slot="header" style="text-align:center">
                <span>请求时间分布图</span>
            </p>
            <distribution-chart :data="chartData"></distribution-chart>
            <div slot="footer">
            </div>
        </Modal>
    </div>
</div>    
</template>
<script>
    import defaultDatePicker from '../components/defaultDatePicker.vue';
    import { distributionChart } from '../components/metricsCharts.vue';
    import { metricsTable } from '../components/metricsTables.vue';

    export default {
        components: {
            'default-date-picker': defaultDatePicker,
            'distribution-chart': distributionChart,
            'metrics-table': metricsTable
        },
        data () {
            const $this = this;
            return {
                dateRange: $this.getInitDateRange(),
                tableData: [],
                chartData: {},
                showDistributionChart: false
            }
        },
        mounted() {
            this.renderTable(...this.dateRange);
        },
        methods: {
            getInitDateRange() {
                const startDate = this.$moment().subtract(7, 'd').format('YYYY-MM-DD');
                const endDate = this.$moment().format('YYYY-MM-DD');
                return [startDate, endDate];
            },
            changeDateRange(dates) {
                this.dateRange = dates;
                this.renderTable(...this.dateRange);
            },
            renderTable(startDate, endDate) {
                const beginTime = this.$moment(startDate, 'YYYY-MM-DD').format('x');
                const endTime = this.$moment(endDate, 'YYYY-MM-DD').format('x');
                const url = `/monitor/metrics/between/list.json?beginTime=${beginTime}&endTime=${endTime}`;
                this.$http.get(url).then(resp => {
                    this.tableData = resp.data.data;
                })
            },
            renderDistributionChart(data) {
                this.showDistributionChart = true;
                const list = data.row.distributionList;
                this.chartData = {
                    title: data.row.pathPattern,
                    xData: list.map(entry => entry.key),
                    yData: list.map(entry => entry.value)
                };
            }
        }
    }
</script>