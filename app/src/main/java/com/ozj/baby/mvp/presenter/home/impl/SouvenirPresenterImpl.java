package com.ozj.baby.mvp.presenter.home.impl;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVUser;
import com.orhanobut.logger.Logger;
import com.ozj.baby.base.BaseView;
import com.ozj.baby.di.scope.ContextLife;
import com.ozj.baby.event.AddSouvenirEvent;
import com.ozj.baby.mvp.model.bean.Souvenir;
import com.ozj.baby.mvp.model.dao.SouvenirDao;
import com.ozj.baby.mvp.model.rx.RxBabyRealm;
import com.ozj.baby.mvp.model.rx.RxBus;
import com.ozj.baby.mvp.model.rx.RxLeanCloud;
import com.ozj.baby.mvp.presenter.home.ISouvenirPresenter;
import com.ozj.baby.mvp.views.home.ISouvenirVIew;
import com.ozj.baby.util.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by Roger on 2016/4/20.
 */
public class SouvenirPresenterImpl implements ISouvenirPresenter {

    private RxLeanCloud mRxleanCloud;
    private RxBus mRxBus;

    private PreferenceManager mPreferencepManager;
    ISouvenirVIew mSouvenirView;

    @Inject
    public SouvenirPresenterImpl(RxLeanCloud rxLeanCloud, PreferenceManager preferenceManager, RxBus rxbus) {
        mRxBus = rxbus;
        mRxleanCloud = rxLeanCloud;
        mPreferencepManager = preferenceManager;
        Logger.init(this.getClass().getSimpleName());
    }


    @Override
    public void AutoLoadingMore() {
        mSouvenirView.showRefreshingLoading();
    }


    @Override
    public void LoadingDataFromNet(final int size, final int page) {
        mSouvenirView.showRefreshingLoading();
        mRxleanCloud.GetALlSouvenirByLeanCloud(mPreferencepManager.getCurrentUserId(), mPreferencepManager.GetLoverID(), size, page)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<AVObject>>() {
                    @Override
                    public void onCompleted() {
                        mSouvenirView.hideRefreshingLoading();

                    }

                    @Override
                    public void onError(Throwable e) {
                        mSouvenirView.hideRefreshingLoading();
                        mSouvenirView.showToast("可能出了点错误哦");
                    }

                    @Override
                    public void onNext(List<AVObject> avObjects) {
                        if (avObjects.size() != 0) {
                            List<Souvenir> list = new ArrayList<>();
                            for (AVObject object : avObjects) {
                                list.add(new Souvenir(object, (AVUser) object.get(SouvenirDao.SOUVENIR_AUTHOR)));
                            }
                            if (page == 0) {
                                mRxBus.post(new AddSouvenirEvent(true, true, list));
                            } else {
                                mRxBus.post(new AddSouvenirEvent(false, true, list));
                            }

                        }
                    }
                });


    }


    @Override
    public void attachView(@NonNull BaseView view) {
        mSouvenirView = (ISouvenirVIew) view;
    }

    @Override
    public void detachView() {

    }
}