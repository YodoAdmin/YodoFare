package co.yodo.fare.ui.option;

import android.view.View;

import java.math.BigDecimal;

import co.yodo.fare.R;
import co.yodo.fare.helper.PrefUtils;
import co.yodo.fare.utils.ErrorUtils;
import co.yodo.fare.manager.PromotionManager;
import co.yodo.fare.ui.FareActivity;
import co.yodo.fare.ui.dialog.BalanceDialog;
import co.yodo.fare.ui.notification.AlertDialogHelper;
import co.yodo.fare.ui.notification.ProgressDialogHelper;
import co.yodo.fare.ui.option.contract.IRequestOption;
import co.yodo.restapi.network.ApiClient;
import co.yodo.restapi.network.model.Params;
import co.yodo.restapi.network.model.ServerResponse;
import co.yodo.restapi.network.request.QueryRequest;

/**
 * Created by hei on 21/06/16.
 * Implements the Balance Option of the Launcher
 */
public class BalanceOption extends IRequestOption {
    /** PIP temporal */
    private String tempPip = null;

    /** Handles the promotions */
    private final PromotionManager promotionManager;
    private boolean isPublishing = false;

    /**
     * Sets up the main elements of the options
     */
    public BalanceOption( final FareActivity activity, final PromotionManager promotionManager ) {
        super( activity );

        // Promotion manager
        this.promotionManager = promotionManager;

        // Dialog
        final View layout = buildLayout();
        final View.OnClickListener okClick = new View.OnClickListener() {
            @Override
            public void onClick( View view  ) {
                setTempPIP( etInput.getText().toString() );

                progressManager.create(
                        activity,
                        ProgressDialogHelper.ProgressDialogType.NORMAL
                );

                requestManager.invoke(
                    new QueryRequest(
                            hardwareToken,
                            tempPip,
                            QueryRequest.Record.HISTORY_BALANCE
                    ),
                    new ApiClient.RequestCallback() {
                        @Override
                        public void onPrepare() {
                            if( PrefUtils.isAdvertising( activity ) ) {
                                promotionManager.unpublish();
                                isPublishing = true;
                            }
                        }

                        @Override
                        public void onResponse( ServerResponse response ) {
                            if( response.getCode().equals( ServerResponse.AUTHORIZED ) ) {
                                alertDialog.dismiss();
                                requestTodayBalance( response.getParams() );
                            } else {
                                progressManager.destroy();
                                tilPip.setError( activity.getString( R.string.error_mip ) );
                            }
                        }

                        @Override
                        public void onError( Throwable error ) {
                            alertDialog.dismiss();
                            progressManager.destroy();
                            ErrorUtils.handleApiError( activity, error, true );

                            // If it was publishing before the request
                            if( isPublishing ) {
                                isPublishing = false;
                                promotionManager.publish();
                            }
                        }
                    }
                );
            }
        };

        alertDialog = AlertDialogHelper.create(
                activity,
                layout,
                buildOnClick( okClick )
        );
    }

    @Override
    public void execute() {
        alertDialog.show();
        clearGUI();
    }

    /**
     * Sets the temporary PIP to a string value
     * @param pip The String PIP
     */
    private void setTempPIP( String pip ) {
        this.tempPip = pip;
    }

    /**
     * Requests the balance for the current day
     * @param totalParams The total balance, used to be displayed in dialog
     */
    private void requestTodayBalance( final Params totalParams ) {
        requestManager.invoke(
            new QueryRequest(
                    hardwareToken,
                    tempPip,
                    QueryRequest.Record.TODAY_BALANCE
            ), new ApiClient.RequestCallback() {
                    @Override
                    public void onPrepare() {

                    }

                    @Override
                public void onResponse( ServerResponse response ) {
                    progressManager.destroy();

                    // Sets all the balance data in the dialog
                    BigDecimal todayBalance = BigDecimal.ZERO;
                    BigDecimal todayCredits = new BigDecimal( response.getParams().getCredit() );
                    BigDecimal todayDebits = new BigDecimal( response.getParams().getDebit() );

                    todayBalance = todayBalance
                            .add( todayCredits )
                            .subtract( todayDebits );

                    BigDecimal historyBalance = BigDecimal.ZERO;
                    BigDecimal historyCredits = new BigDecimal( totalParams.getCredit() );
                    BigDecimal historyDebits = new BigDecimal( totalParams.getDebit() );

                    historyBalance = historyBalance
                            .add( historyCredits )
                            .subtract( historyDebits );

                    new BalanceDialog.Builder( activity )
                            .todayCredits( todayCredits.toString() )
                            .todayDebits( todayDebits.toString() )
                            .todayBalance( todayBalance.toString() )
                            .historyCredits( historyCredits.toString() )
                            .historyDebits( historyDebits.toString() )
                            .historyBalance( historyBalance.toString() )
                            .build();

                    // If it was publishing before the request
                    if( isPublishing ) {
                        isPublishing = false;
                        promotionManager.publish();
                    }
                }

                @Override
                public void onError( Throwable error ) {
                    progressManager.destroy();
                    ErrorUtils.handleApiError( activity, error, true );
                }
            }
        );
    }
}
