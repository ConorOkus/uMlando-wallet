package com.example.umlandowallet

import org.ldk.enums.ConfirmationTarget
import org.ldk.structs.FeeEstimator

// To create a FeeEstimator we need to provide an object that implements the FeeEstimatorInterface
// which has 1 function: get_est_sat_per_1000_weight(conf_target: ConfirmationTarget?): Int
object LDKFeeEstimator : FeeEstimator.FeeEstimatorInterface {
    override fun get_est_sat_per_1000_weight(confirmationTarget: ConfirmationTarget?): Int {
        if (confirmationTarget == ConfirmationTarget.LDKConfirmationTarget_OnChainSweep) {
            return 7500
        }

        if (confirmationTarget == ConfirmationTarget.LDKConfirmationTarget_ChannelCloseMinimum) {
            return 1000
        }

        if (confirmationTarget == ConfirmationTarget.LDKConfirmationTarget_AnchorChannelFee) {
            return 253
        }

        if (confirmationTarget == ConfirmationTarget.LDKConfirmationTarget_NonAnchorChannelFee) {
            return 7500
        }

        return 253
    }
}