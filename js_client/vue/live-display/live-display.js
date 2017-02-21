
function url(s) {
    const l = window.location;
    return ((l.protocol === "https:") ? "wss://" : "ws://") + l.host + s;
}

let socket = false;

module.exports = {
    data: function() {
        return {
            vms: {},
            running: false,
            paused: false,
            messages: 0,
            experience: [[]]
        };
    },

    name: "live-display",
    props: ["mode", "sla", "templates"],

    computed: {
        numVMs: function () {
            return Object.keys(this.vms).length;
        }
    },

    methods: {

        pause: function() {
            if (!socket) return;
            var msg = {
                type: "pause"
            };
            socket.send(JSON.stringify(msg));
            this.paused = true;
        },

        play: function() {
            if (!socket) return;
            var msg = {
                type: "play"
            };
            socket.send(JSON.stringify(msg));
            this.paused = false;
        },

        
        provisionVM: function (vmID, vmType) {
            this.$set(this.vms, vmID, {"id": vmID,
                                       "state": "starting",
                                       "queue": [],
                                       "type": vmType,
                                       "offAt": false,
                                       "shouldDisplay": true});
        },

        showExperience: function(vmID) {
            let msg = {
                "type": "features",
                "id": vmID
            };
            socket.send(JSON.stringify(msg));
            console.log("send feature!");
        },

        markVMReady: function (vmID) {
            this.vms[vmID].state = "running";
        },

        shutdownVM: function (vmID) {
            this.vms[vmID].state = "off";
            this.vms[vmID].offAt = false;
        },

        assignQuery: function (vmID, queryID, template) {
            this.vms[vmID].queue.push({queryID, template});
            this.vms[vmID].offAt = false;
            this.vms[vmID].shouldDisplay = true;
        },

        queryComplete: function (queryID) {
            // TODO this scans all VMs looking for the query. we could
            // keep a mapping.
            for (let k in this.vms) {
                this.vms[k].queue = this.vms[k].queue
                    .filter(q => q.queryID != queryID);
            }
        },

        noteCost: function (tick, cost, clairvoyant) {
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

            this.$emit("rlearn-cost-update", {tick, cost, clairvoyant});
        },

        start: function() {
            socket = new WebSocket(url("/bandit"));
            var msg = {
                type: "setUp",
                templates: this.templates,
                deadline: this.sla
            };
            socket.onopen = (event) => {
                socket.send(JSON.stringify(msg));
            };

            socket.onmessage = (event) => {
                let data = event.data;
                this.messages += 1;
                data = JSON.parse(data);
                const msgType = data["type"];
                switch (msgType) {
                case "assign":
                    this.assignQuery(data.vmID, data.queryID, data.template);
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
                    this.noteCost(data.tick, data.cost, false);
                    break;
                case "clairvoyantCost":
                    this.noteCost(data.tick, data.cost, true);
                    break;
                case "features":
                    this.experience = data["experience"];
                    $("#expModal").modal('show');
                    console.log(JSON.stringify(this.experience));
                    break;
                default:
                    console.log("got unknown message: " + JSON.stringify(data));
                }

            };
            this.running = true;
        },

        stop: function() {
            if (socket)
                socket.close();
            this.running = false;
            this.vms = {};
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
