package org.dodgybits.shuffle.gwt.core;

import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.annotations.NameToken;
import org.dodgybits.shuffle.gwt.place.NameTokens;

import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.Request;
import com.google.web.bindery.requestfactory.shared.ServerFailure;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.RevealContentEvent;
import org.dodgybits.shuffle.shared.TaskProxy;
import org.dodgybits.shuffle.shared.TaskService;

public class EditActionPresenter extends
        Presenter<EditActionPresenter.MyView, EditActionPresenter.MyProxy>
        implements EditEntityUiHandlers {


    private enum Action {
        NEW, EDIT
    }

    public interface MyView extends View, HasUiHandlers<EditEntityUiHandlers> {
        void displayTask(TaskProxy task);
    }

    @ProxyCodeSplit
    @NameToken(NameTokens.editAction)
    public interface MyProxy extends ProxyPlace<EditActionPresenter> {
    }

    private final TaskService mTaskService;
    private PlaceManager placeManager;
    private Action mAction;
    private TaskProxy mTask = null;

    @Inject
    public EditActionPresenter(final EventBus eventBus, final MyView view,
                               final MyProxy proxy, final PlaceManager placeManager, final Provider<TaskService> taskServiceProvider) {
        super(eventBus, view, proxy);
        this.placeManager = placeManager;

        this.mTaskService = taskServiceProvider.get();
        getView().setUiHandlers(this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest placeRequest) {
        super.prepareFromRequest(placeRequest);

        // In the next call, "view" is the default value,
        // returned if "action" is not found on the URL.
        String actionString = placeRequest.getParameter("action", "new");
        mAction = Action.NEW;
        if ("edit".equals(actionString)) {
            Long taskId = null;
            mAction = Action.EDIT;
            try {
                taskId = Long.valueOf(placeRequest.getParameter("taskId", null));
            } catch (NumberFormatException e) {
            }

            if (taskId == null) {
                placeManager.revealErrorPlace(placeRequest.getNameToken());
                return;
            }

            load(taskId);
        }
    }

    @Override
    protected void revealInParent() {
        RevealContentEvent.fire(this, MainPresenter.MAIN_SLOT, this);
    }


    private void load(Long taskId) {
        // Send a message using RequestFactory
        Request<TaskProxy> taskListRequest = mTaskService.findById(taskId);
        taskListRequest.fire(new Receiver<TaskProxy>() {
            @Override
            public void onFailure(ServerFailure error) {
                GWT.log(error.getMessage());
            }

            @Override
            public void onSuccess(TaskProxy task) {
                mTask = task;
                GWT.log("Success - got " + task);
                getView().displayTask(task);
            }
        });
    }

    @Override
    public void save(String description, String details) {
        if (mAction == Action.NEW)
        {
            mTask = mTaskService.create(TaskProxy.class);
        }

        mTask.setDescription(description);
        mTask.setDetails(details);

        Request<Void> saveRequest = mTaskService.save(mTask);
        saveRequest.fire(new Receiver<Void>() {
            @Override
            public void onFailure(ServerFailure error) {
                GWT.log(error.getMessage());
            }
            @Override
            public void onSuccess(Void response) {
                GWT.log("Success");
                goBack();
            }
        });
    }

    @Override
    public void cancel() {
        goBack();
    }

    private void goBack() {
        // TODO - go back to previous page
    }

}