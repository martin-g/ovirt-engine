package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetAllNetworkQueryParamenters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.VdsGroupQueryParamenters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Cloner;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.common.SelectionTreeNodeModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class DataCenterNetworkListModel extends SearchableListModel implements IFrontendMultipleQueryAsyncCallback
{
    private static String ENGINE_NETWORK;

    private UICommand privateNewCommand;

    public UICommand getNewCommand()
    {
        return privateNewCommand;
    }

    private void setNewCommand(UICommand value)
    {
        privateNewCommand = value;
    }

    private UICommand privateEditCommand;

    public UICommand getEditCommand()
    {
        return privateEditCommand;
    }

    private void setEditCommand(UICommand value)
    {
        privateEditCommand = value;
    }

    private UICommand privateRemoveCommand;

    public UICommand getRemoveCommand()
    {
        return privateRemoveCommand;
    }

    private void setRemoveCommand(UICommand value)
    {
        privateRemoveCommand = value;
    }

    @Override
    public storage_pool getEntity()
    {
        return (storage_pool) super.getEntity();
    }

    public void setEntity(storage_pool value)
    {
        super.setEntity(value);
    }

    private ArrayList<VDSGroup> privateClusterList;

    public ArrayList<VDSGroup> getClusterList()
    {
        return privateClusterList;
    }

    public void setClusterList(ArrayList<VDSGroup> value)
    {
        privateClusterList = value;
    }

    private ArrayList<SelectionTreeNodeModel> privateSelectionNodeList;

    public ArrayList<SelectionTreeNodeModel> getSelectionNodeList()
    {
        return privateSelectionNodeList;
    }

    public void setSelectionNodeList(ArrayList<SelectionTreeNodeModel> value)
    {
        privateSelectionNodeList = value;
    }

    public DataCenterNetworkListModel()
    {
        // get management network name
        AsyncDataProvider.GetManagementNetworkName(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {
                ENGINE_NETWORK = (String) returnValue;
                UpdateActionAvailability();
            }
        }));

        setTitle(ConstantsManager.getInstance().getConstants().logicalNetworksTitle());
        setHashName("logical_networks"); //$NON-NLS-1$

        setNewCommand(new UICommand("New", this)); //$NON-NLS-1$
        setEditCommand(new UICommand("Edit", this)); //$NON-NLS-1$
        setRemoveCommand(new UICommand("Remove", this)); //$NON-NLS-1$

        UpdateActionAvailability();
    }

    @Override
    protected void OnEntityChanged()
    {
        super.OnEntityChanged();
        getSearchCommand().Execute();
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            super.Search();
        }
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        super.SyncSearch();

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                SearchableListModel searchableListModel = (SearchableListModel) model;
                searchableListModel.setItems((ArrayList<network>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        GetAllNetworkQueryParamenters tempVar = new GetAllNetworkQueryParamenters(getEntity().getId());
        tempVar.setRefresh(getIsQueryFirstTime());
        Frontend.RunQuery(VdcQueryType.GetAllNetworks, tempVar, _asyncQuery);
    }

    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();

        setAsyncResult(Frontend.RegisterQuery(VdcQueryType.GetAllNetworks,
                new GetAllNetworkQueryParamenters(getEntity().getId())));
        setItems(getAsyncResult().getData());
    }

    public void remove()
    {
        if (getWindow() != null)
        {
            return;
        }

        ConfirmationModel model = new ConfirmationModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().removeLogicalNetworkTitle());
        model.setHashName("remove_logical_network"); //$NON-NLS-1$
        model.setMessage(ConstantsManager.getInstance().getConstants().logicalNetworksMsg());

        ArrayList<String> list = new ArrayList<String>();
        for (network a : Linq.<network> Cast(getSelectedItems()))
        {
            list.add(a.getname());
        }
        model.setItems(list);

        UICommand tempVar = new UICommand("OnRemove", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        model.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        model.getCommands().add(tempVar2);
    }

    public void OnRemove()
    {
        ArrayList<VdcActionParametersBase> pb = new ArrayList<VdcActionParametersBase>();
        for (network a : Linq.<network> Cast(getSelectedItems()))
        {
            pb.add(new AddNetworkStoragePoolParameters(getEntity().getId(), a));
        }
        Frontend.RunMultipleAction(VdcActionType.RemoveNetwork, pb);

        Cancel();
    }

    public void Edit()
    {
        network network = (network) getSelectedItem();

        if (getWindow() != null)
        {
            return;
        }

        DataCenterNetworkModel networkModel = new DataCenterNetworkModel();
        setWindow(networkModel);
        networkModel.setTitle(ConstantsManager.getInstance().getConstants().editLogicalNetworkTitle());
        networkModel.setHashName("edit_logical_network"); //$NON-NLS-1$
        networkModel.getName().setEntity(network.getname());
        networkModel.getDescription().setEntity(network.getdescription());
        networkModel.getIsStpEnabled().setEntity(network.getstp());
        networkModel.getHasVLanTag().setEntity(network.getvlan_id() != null);
        networkModel.getVLanTag().setEntity((network.getvlan_id() == null ? 0 : network.getvlan_id()));
        networkModel.getHasMtu().setEntity(network.getMtu() != 0);
        networkModel.getMtu().setEntity(network.getMtu() != 0 ? String.valueOf(network.getMtu()) : null);
        networkModel.getIsVmNetwork().setEntity(network.isVmNetwork());

        networkModel.setDetachAllCommand(new UICommand("DetachClusters", this)); //$NON-NLS-1$
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                DataCenterNetworkListModel dcNetworkModel = (DataCenterNetworkListModel) model;
                dcNetworkModel.setClusterList((ArrayList<VDSGroup>) ReturnValue);
                dcNetworkModel.setSelectionNodeList(new ArrayList<SelectionTreeNodeModel>());
                ArrayList<VdcQueryParametersBase> parametersList =
                        new ArrayList<VdcQueryParametersBase>();
                ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
                for (VDSGroup vdsGroup : dcNetworkModel.getClusterList())
                {
                    queryTypeList.add(VdcQueryType.GetAllNetworksByClusterId);
                    parametersList.add(new VdsGroupQueryParamenters(vdsGroup.getId()));
                    SelectionTreeNodeModel tempVar = new SelectionTreeNodeModel();
                    tempVar.setIsSelectedNullable(false);
                    tempVar.setEntity(vdsGroup);
                    tempVar.setDescription(vdsGroup.getname());
                    dcNetworkModel.getSelectionNodeList().add(tempVar);
                }
                Frontend.RunMultipleQueries(queryTypeList, parametersList, dcNetworkModel);
                DataCenterNetworkModel networkModel1 = (DataCenterNetworkModel) dcNetworkModel.getWindow();

                // cannot detach engine networks from clusters
                network network1 = (network) dcNetworkModel.getSelectedItem();
                if (StringHelper.stringsEqual(network1.getname(), ENGINE_NETWORK))
                {
                    for (SelectionTreeNodeModel nodeModel : dcNetworkModel.getSelectionNodeList())
                    {
                        nodeModel.setIsChangable(false);
                    }
                    networkModel1.getDetachAllCommand().setIsAvailable(false);
                    networkModel1.getName().setIsChangable(false);
                    networkModel1.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .cannotDetachManagementNetworkFromClustersMsg());
                }
            }
        };
        AsyncDataProvider.GetClusterList(_asyncQuery, getEntity().getId());
    }

    public void New()
    {
        if (getWindow() != null)
        {
            return;
        }

        DataCenterNetworkModel networkModel = new DataCenterNetworkModel();
        setWindow(networkModel);
        networkModel.setTitle(ConstantsManager.getInstance().getConstants().newLogicalNetworkTitle());
        networkModel.setHashName("new_logical_network"); //$NON-NLS-1$
        networkModel.setIsNew(true);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                DataCenterNetworkListModel networkListModel = (DataCenterNetworkListModel) model;
                DataCenterNetworkModel networkModel1 = (DataCenterNetworkModel) networkListModel.getWindow();
                // networkModel1.ClusterTreeNodes
                ArrayList<VDSGroup> clusterList = (ArrayList<VDSGroup>) ReturnValue;
                SelectionTreeNodeModel nodeModel;
                ArrayList<SelectionTreeNodeModel> clusterTreeNodes =
                        new ArrayList<SelectionTreeNodeModel>();
                for (VDSGroup selectionTreeNodeModel : clusterList)
                {
                    nodeModel = new SelectionTreeNodeModel();
                    nodeModel.setEntity(selectionTreeNodeModel);
                    nodeModel.setDescription(selectionTreeNodeModel.getname());
                    nodeModel.setIsSelectedNullable(false);
                    clusterTreeNodes.add(nodeModel);
                }
                networkModel1.setClusterTreeNodes(clusterTreeNodes);

                UICommand tempVar = new UICommand("OnSave", networkListModel); //$NON-NLS-1$
                tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
                tempVar.setIsDefault(true);
                networkModel1.getCommands().add(tempVar);
                networkModel1.setDetachAllCommand(new UICommand("DetachClusters", networkListModel)); //$NON-NLS-1$
                networkModel1.getDetachAllAvailable().setEntity(false);
                UICommand tempVar2 = new UICommand("Cancel", networkListModel); //$NON-NLS-1$
                tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
                tempVar2.setIsCancel(true);
                networkModel1.getCommands().add(tempVar2);
            }
        };
        AsyncDataProvider.GetClusterList(_asyncQuery, getEntity().getId());
    }

    public void OnSave()
    {
        DataCenterNetworkModel model = (DataCenterNetworkModel) getWindow();

        if (getEntity() == null || (!model.getIsNew() && getSelectedItem() == null))
        {
            Cancel();
            return;
        }

        model.setcurrentNetwork(model.getIsNew() ? new network(null) : (network) Cloner.clone(getSelectedItem()));

        if (!model.Validate())
        {
            return;
        }

        // Save changes.
        model.getcurrentNetwork().setstorage_pool_id(getEntity().getId());
        model.getcurrentNetwork().setname((String) model.getName().getEntity());
        model.getcurrentNetwork().setstp((Boolean) model.getIsStpEnabled().getEntity());
        model.getcurrentNetwork().setdescription((String) model.getDescription().getEntity());
        model.getcurrentNetwork().setVmNetwork((Boolean) model.getIsVmNetwork().getEntity());

        model.getcurrentNetwork().setMtu(0);
        if (model.getMtu().getEntity() != null)
        {
            model.getcurrentNetwork().setMtu(Integer.parseInt(model.getMtu().getEntity().toString()));
        }

        model.getcurrentNetwork().setvlan_id(null);
        if ((Boolean) model.getHasVLanTag().getEntity())
        {
            model.getcurrentNetwork().setvlan_id(Integer.parseInt(model.getVLanTag().getEntity().toString()));
        }

        model.setnewClusters(new ArrayList<VDSGroup>());

        for (SelectionTreeNodeModel selectionTreeNodeModel : model.getClusterTreeNodes())
        {
            if (selectionTreeNodeModel.getIsSelectedNullable() != null
                    && selectionTreeNodeModel.getIsSelectedNullable().equals(true))
            {
                model.getnewClusters().add((VDSGroup) selectionTreeNodeModel.getEntity());
            }
        }
        ArrayList<VDSGroup> detachNetworkFromClusters =
                Linq.Except(model.getOriginalClusters(), model.getnewClusters());
        ArrayList<VdcActionParametersBase> actionParameters =
                new ArrayList<VdcActionParametersBase>();

        for (VDSGroup detachNetworkFromCluster : detachNetworkFromClusters)
        {
            actionParameters.add(new AttachNetworkToVdsGroupParameter(detachNetworkFromCluster,
                    model.getcurrentNetwork()));
        }

        model.StartProgress(null);

        Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, actionParameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        DataCenterNetworkListModel networkListModel = (DataCenterNetworkListModel) result.getState();
                        DataCenterNetworkModel networkModel = (DataCenterNetworkModel) networkListModel.getWindow();
                        network network = networkModel.getcurrentNetwork();
                        if (networkModel.getIsNew())
                        {
                            Frontend.RunAction(VdcActionType.AddNetwork,
                                    new AddNetworkStoragePoolParameters(getEntity().getId(), network),
                                    new IFrontendActionAsyncCallback() {
                                        @Override
                                        public void Executed(FrontendActionAsyncResult result1) {

                                            DataCenterNetworkListModel networkListModel1 =
                                                    (DataCenterNetworkListModel) result1.getState();
                                            VdcReturnValueBase retVal = result1.getReturnValue();
                                            boolean succeeded = false;
                                            if (retVal != null && retVal.getSucceeded())
                                            {
                                                succeeded = true;
                                            }
                                            networkListModel1.PostNetworkAction(succeeded ? (Guid) retVal.getActionReturnValue()
                                                    : null,
                                                    succeeded);

                                        }
                                    },
                                    networkListModel);
                        }
                        else
                        {
                            if ((Boolean) networkModel.getIsEnabled().getEntity())
                            {
                                Frontend.RunAction(VdcActionType.UpdateNetwork,
                                        new AddNetworkStoragePoolParameters(getEntity().getId(), network),
                                        new IFrontendActionAsyncCallback() {
                                            @Override
                                            public void Executed(FrontendActionAsyncResult result1) {

                                                DataCenterNetworkListModel networkListModel1 =
                                                        (DataCenterNetworkListModel) result1.getState();
                                                VdcReturnValueBase retVal = result1.getReturnValue();
                                                networkListModel1.PostNetworkAction(null,
                                                        retVal != null && retVal.getSucceeded());

                                            }
                                        },
                                        networkListModel);
                            }
                            else
                            {
                                PostNetworkAction(null, true);
                            }
                        }

                    }
                },
                this);
    }

    public void PostNetworkAction(Guid networkGuid, boolean succeeded)
    {
        DataCenterNetworkModel networkModel = (DataCenterNetworkModel) getWindow();
        if (succeeded)
        {
            Cancel();
        }
        else
        {
            networkModel.StopProgress();
            return;
        }
        networkModel.StopProgress();
        network network = networkModel.getcurrentNetwork();
        Guid networkId = networkModel.getIsNew() ? networkGuid : network.getId();
        ArrayList<VDSGroup> attachNetworkToClusters =
                Linq.Except(networkModel.getnewClusters(), networkModel.getOriginalClusters());
        ArrayList<VdcActionParametersBase> actionParameters1 =
                new ArrayList<VdcActionParametersBase>();

        for (VDSGroup attachNetworkToCluster : attachNetworkToClusters)
        {
            network tempVar = new network(null);
            tempVar.setId(networkId);
            tempVar.setname(network.getname());
            actionParameters1.add(new AttachNetworkToVdsGroupParameter(attachNetworkToCluster, tempVar));
        }

        Frontend.RunMultipleAction(VdcActionType.AttachNetworkToVdsGroup, actionParameters1);
    }

    public void DetachClusters()
    {
        ConfirmationModel confirmModel = new ConfirmationModel();
        setConfirmWindow(confirmModel);
        confirmModel.setTitle(ConstantsManager.getInstance().getConstants().detachNetworkFromAllClustersTitle());
        confirmModel.setHashName("detach_network_from_all_clusters"); //$NON-NLS-1$
        confirmModel.setMessage(ConstantsManager.getInstance()
                .getConstants()
                .youAreAboutToDetachNetworkFromAllClustersToWhichCurrentlyAttachedMsg());
        confirmModel.getLatch().setIsAvailable(true);
        UICommand tempVar = new UICommand("OnDetachClusters", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().ok());
        tempVar.setIsDefault(true);
        confirmModel.getCommands().add(tempVar);
        UICommand tempVar2 = new UICommand("CancelConfirmation", this); //$NON-NLS-1$
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().cancel());
        tempVar2.setIsCancel(true);
        confirmModel.getCommands().add(tempVar2);
    }

    public void CancelConfirmation()
    {
        setConfirmWindow(null);
    }

    public void OnDetachClusters()
    {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();

        if (!confirmationModel.Validate())
        {
            return;
        }
        DataCenterNetworkModel model = (DataCenterNetworkModel) getWindow();
        network network = (network) getSelectedItem();

        ArrayList<VdcActionParametersBase> actionParameters =
                new ArrayList<VdcActionParametersBase>();

        for (SelectionTreeNodeModel selectionTreeNodeModel : model.getClusterTreeNodes())
        {
            if (selectionTreeNodeModel.getIsSelectedNullable() != null
                    && selectionTreeNodeModel.getIsSelectedNullable().equals(true))
            {
                selectionTreeNodeModel.setIsSelectedNullable(false);
                actionParameters.add(new AttachNetworkToVdsGroupParameter((VDSGroup) selectionTreeNodeModel.getEntity(),
                        network));
            }
        }
        Frontend.RunMultipleAction(VdcActionType.DetachNetworkToVdsGroup, actionParameters,
                new IFrontendMultipleActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendMultipleActionAsyncResult result) {

                        DataCenterNetworkListModel networkListModel = (DataCenterNetworkListModel) result.getState();
                        DataCenterNetworkModel networkModel = (DataCenterNetworkModel) networkListModel.getWindow();
                        boolean isSucceded = true;
                        networkListModel.CancelConfirmation();
                        if (isSucceded)
                        {
                            networkModel.setOriginalClusters(new ArrayList<VDSGroup>());
                            networkModel.getIsEnabled().setEntity(true);
                            networkModel.getDetachAllAvailable().setEntity(!(Boolean) networkModel.getIsEnabled()
                                    .getEntity());
                        }
                        else
                        {
                            networkListModel.Cancel();
                        }

                    }
                },
                this);
    }

    public void Cancel()
    {
        setWindow(null);
    }

    @Override
    protected void OnSelectedItemChanged()
    {
        super.OnSelectedItemChanged();
        UpdateActionAvailability();
    }

    @Override
    protected void SelectedItemsChanged()
    {
        super.SelectedItemsChanged();
        UpdateActionAvailability();
    }

    private void UpdateActionAvailability()
    {
        List tempVar = getSelectedItems();
        ArrayList selectedItems =
                (ArrayList) ((tempVar != null) ? tempVar : new ArrayList());

        boolean anyEngine = false;
        for (Object item : selectedItems)
        {
            network network = (network) item;
            if (StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK))
            {
                anyEngine = true;
                break;
            }
        }

        getEditCommand().setIsExecutionAllowed(selectedItems.size() == 1);
        getRemoveCommand().setIsExecutionAllowed(selectedItems.size() > 0 && !anyEngine);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getNewCommand())
        {
            New();
        }
        else if (command == getEditCommand())
        {
            Edit();
        }
        else if (command == getRemoveCommand())
        {
            remove();
        }

        else if (StringHelper.stringsEqual(command.getName(), "OnSave")) //$NON-NLS-1$
        {
            OnSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnRemove")) //$NON-NLS-1$
        {
            OnRemove();
        }
        else if (StringHelper.stringsEqual(command.getName(), "DetachClusters")) //$NON-NLS-1$
        {
            DetachClusters();
        }
        else if (StringHelper.stringsEqual(command.getName(), "OnDetachClusters")) //$NON-NLS-1$
        {
            OnDetachClusters();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirmation")) //$NON-NLS-1$
        {
            CancelConfirmation();
        }
    }

    @Override
    public void Executed(FrontendMultipleQueryAsyncResult result)
    {
        network network = (network) getSelectedItem();
        List<VdcQueryReturnValue> returnValueList = result.getReturnValues();
        DataCenterNetworkModel model = (DataCenterNetworkModel) getWindow();
        ArrayList<network> clusterNetworkList = null;
        boolean networkHasAttachedClusters = false;
        for (int i = 0; i < returnValueList.size(); i++)
        {
            VdcQueryReturnValue returnValue = returnValueList.get(i);
            if (returnValue.getSucceeded() && returnValue.getReturnValue() != null)
            {
                clusterNetworkList = (ArrayList<network>) returnValue.getReturnValue();
                for (network clusterNetwork : clusterNetworkList)
                {
                    if (clusterNetwork.getId().equals(network.getId()))
                    {
                        model.getOriginalClusters().add((VDSGroup) getSelectionNodeList().get(i).getEntity());
                        getSelectionNodeList().get(i).setIsSelectedNullable(true);
                        networkHasAttachedClusters = true;
                        break;
                    }
                }
            }
        }
        if (networkHasAttachedClusters)
        {
            model.getIsEnabled().setEntity(false);
            if (!StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK))
            {
                model.getDetachAllAvailable().setEntity(!(Boolean) model.getIsEnabled().getEntity());
            }
        }

        model.setClusterTreeNodes(getSelectionNodeList());
        if (StringHelper.stringsEqual(network.getname(), ENGINE_NETWORK) && getSelectionNodeList().size() > 0)
        {
            UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
            tempVar.setIsDefault(true);
            tempVar.setIsCancel(true);
            model.getCommands().add(tempVar);
        }
        else
        {
            UICommand tempVar2 = new UICommand("OnSave", this); //$NON-NLS-1$
            tempVar2.setTitle(ConstantsManager.getInstance().getConstants().ok());
            tempVar2.setIsDefault(true);
            model.getCommands().add(tempVar2);
            UICommand tempVar3 = new UICommand("Cancel", this); //$NON-NLS-1$
            tempVar3.setTitle(ConstantsManager.getInstance().getConstants().cancel());
            tempVar3.setIsCancel(true);
            model.getCommands().add(tempVar3);
        }
    }

    @Override
    protected String getListName() {
        return "DataCenterNetworkListModel"; //$NON-NLS-1$
    }

}
