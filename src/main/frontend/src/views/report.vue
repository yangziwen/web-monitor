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
        from:
        <Date-picker type="date" placeholder="请选择日期"
            :value="dateTimeRange.begin.date" format="yyyy-MM-dd"
            style="width: 120px; display: inline-block;"
            @on-change="onBeginDateChange"
        ></Date-picker>
        <Time-picker type="time" placeholder="选择时间"
            :value="dateTimeRange.begin.time" format="HH:mm"
            style="width: 100px; display: inline-block;"
            @on-change="onBeginTimeChange"
        ></Time-picker>
        &nbsp;&nbsp;
        <div style="margin-top: 10px; display: inline-block;">
        最近
        <RadioGroup v-model="shortcutButton" type="button"
            @on-change="onShortcutButtonChange">
            <Radio label="10m"></Radio>
            <Radio label="30m"></Radio>
            <Radio label="1h"></Radio>
            <Radio label="2h"></Radio>
            <Radio label="6h"></Radio>
            <Radio label="12h"></Radio>
            <Radio label="1d"></Radio>
            <Radio label="7d"></Radio>
        </RadioGroup>
        </div>
    </div>
    <div>
        <report-table :data="tableData"></report-table>
    </div>
</div>
</template>
<script>
    import { distributionChart } from '../components/metricsCharts.vue';
    import { reportTable } from '../components/metricsTables.vue';

    export default {
        components: {
            'distribution-chart': distributionChart,
            'report-table': reportTable
        },
        data () {
            const $this = this;
            return {
                shortcutButton: '10m',
                dateTimeRange: {
                    begin: {
                        date: '',
                        time: ''
                    }
                },
                tableData: []
            }
        },
        computed: {
            beginDateTime() {
                if (!this.dateTimeRange || !this.dateTimeRange.begin) {
                    return '';
                }
                const { date, time } = this.dateTimeRange.begin;
                return date + ' ' + time;
            }
        },
        mounted() {
            this.changeDateTimeRange(Date.now() - 1000 * 60 * 10);
            this.renderTable();
        },
        methods: {
            changeDateTimeRange(beginMilliSeconds) {
                const beginDateTime = this.$moment(beginMilliSeconds);
                this.dateTimeRange = {
                    begin: {
                        date: beginDateTime.format('YYYY-MM-DD'),
                        time: beginDateTime.format('HH:mm')
                    }
                };
            },
            onShortcutButtonChange(value) {
                const matchedResult = /(\d+)([mhd])/.exec(value);
                if (!matchedResult) {
                    return;
                }
                const units = {
                    m: 60 * 1000,
                    h: 60 * 60 * 1000,
                    d: 24 * 60 * 60 * 1000
                };
                const beginMilliSeconds = Date.now() - units[matchedResult[2]] * matchedResult[1];
                this.changeDateTimeRange(beginMilliSeconds);
                this.renderTable();
            },
            onBeginDateChange(date) {
                this.dateTimeRange.begin.date = date;
                this.shortcutButton = '';
                this.renderTable();
            },
            onBeginTimeChange(time) {
                this.dateTimeRange.begin.time = time;
                this.shortcutButton = '';
                this.renderTable();
            },
            renderTable() {
                const beginTime = this.$moment(this.beginDateTime, 'YYYY-MM-DD HH:mm').format('x');
                const url = `/monitor/metrics/recent/report.json?beginTime=${beginTime}`;
                this.$http.get(url).then(resp => {
                    this.tableData = resp.data.data.details;
                });
            }
        }
    }
</script>