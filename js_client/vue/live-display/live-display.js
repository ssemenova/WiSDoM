
function url(s) {
    const l = window.location;
    return ((l.protocol === "https:") ? "wss://" : "ws://") + l.host + s;
}

let socket = false;

module.exports = {
    data: function() {
        return {
            vms: {},
            running: false
        };
    },

    name: "live-display",
    props: ["mode", "sla"],

    methods: {
        provisionVM: function (vmID, vmType) {
            this.$set(this.vms, vmID, {"id": vmID,
                                       "state": "starting",
                                       "queue": [],
                                       "type": vmType,
                                       "offAt": false,
                                       "shouldDisplay": true});
        },

        markVMReady: function (vmID) {
            this.vms[vmID].state = "running";
        },

        shutdownVM: function (vmID) {
            this.vms[vmID].state = "off";
            this.vms[vmID].offAt = false;
        },

        assignQuery: function (vmID, queryID) {
            this.vms[vmID].queue.push(queryID);
            this.vms[vmID].offAt = false;
            this.vms[vmID].shouldDisplay = true;
        },

        queryComplete: function (queryID) {
            // TODO this scans all VMs looking for the query. we could
            // keep a mapping.
            for (let k in this.vms) {
                this.vms[k].queue = this.vms[k].queue.filter(q => q != queryID);
            }
        },

        noteCost: function (tick, cost) {
            // check for off VMs...
            for (let vmID in this.vms) {
                if (this.vms[vmID].state == "off") {
                    if (this.vms[vmID].offAt == false) {
                        this.vms[vmID].offAt = tick;
                    }

                    if (tick - this.vms[vmID].offAt >= 50) {
                        this.vms[vmID].shouldDisplay = false;
                    }
                }
            }

            console.log("at " + tick + " cost is " + cost);
        },

        start: function() {
            socket = new WebSocket(url("/bandit"));
            socket.onmessage = (event) => {
                let data = event.data;
                data = JSON.parse(data);
                const msgType = data["type"];
                switch (msgType) {
                case "assign":
                    this.assignQuery(data.vmID, data.queryID);
                    break;
                case "complete":
                    this.queryComplete(data.queryID);
                    break;
                case "provision":
                    this.provisionVM(data.vmID, data.vmType);
                    break;
                case "ready":
                    this.markVMReady(data.vmID);
                    break;
                case "shutdown":
                    this.shutdownVM(data.vmID);
                    break;
                case "cost":
                    this.noteCost(data.tick, data.cost);
                    break;
                }

            };
            this.running = true;
        },

        stop: function() {
            if (socket)
                socket.close();
            this.running = false;
        },

        startIfCorrectMode: function () {
            if (this.mode == "rlearn" && this.sla != false) {
                this.start();
            } else {
                this.stop();
            }
        }
    },

    watch: {
        mode: function () { this.startIfCorrectMode(); },
        sla: function () { this.startIfCorrectMode(); }
    },

    mounted: function() { this.startIfCorrectMode(); },
    beforeDestroy: function() { this.stop(); }
    

    
};
