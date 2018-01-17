"use strict";

// Similar to the IOU creation modal - see createIOUModal.js for comments.
angular.module('demoAppModule').controller('TransferModalCtrl', function ($http, $uibModalInstance, $uibModal, apiBaseURL, peers, id,selectedItem) {
    const transferModal = this;

    console.log(selectedItem.ticker);
    
    transferModal.peers = peers;
    transferModal.id = id;
    transferModal.form = {};
    transferModal.formError = false;
    transferModal.item =selectedItem;
    transferModal.item.units = 0;
    console.log("calling URL11::"+transferModal.item.marketValue);
    transferModal.transfer = () => {

            const id = transferModal.id;
            const party = transferModal.form.counterparty;

            $uibModalInstance.close();

            const issueIOUEndpoint =
                apiBaseURL +
                `transfer-obligation?id=${id}&party=${party}`;
            //ticker=tick1&quantity=123
            console.log("calling URL::"+transferModal.item.units);
            $http.get("/api/order/create?ticker="+transferModal.item.ticker+"&quantity="+transferModal.item.marketValue).then(
                (result) => transferModal.displayMessage(result),
                (result) => transferModal.displayMessage(result)
            );
        
    };

    transferModal.displayMessage = (message) => {
        const transferMsgModal = $uibModal.open({
            templateUrl: 'transferMsgModal.html',
            controller: 'transferMsgModalCtrl',
            controllerAs: 'transferMsgModal',
            resolve: { message: () => message }
        });

        transferMsgModal.result.then(() => {}, () => {});
    };

    transferModal.cancel = () => $uibModalInstance.dismiss();

    function invalidFormInput() {
        return transferModal.form.counterparty === undefined;
    }
});

angular.module('demoAppModule').controller('transferMsgModalCtrl', function ($uibModalInstance, message) {
    const transferMsgModal = this;
    transferMsgModal.message = message.data;
});