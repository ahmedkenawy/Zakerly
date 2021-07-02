package com.graduationproject.zakerly.navigation.notifications;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.database.FirebaseDatabase;
import com.graduationproject.zakerly.R;
import com.graduationproject.zakerly.core.constants.NotificationType;
import com.graduationproject.zakerly.core.constants.RequestStatus;
import com.graduationproject.zakerly.core.models.ConnectionModel;
import com.graduationproject.zakerly.core.models.Instructor;
import com.graduationproject.zakerly.core.models.NotificationData;
import com.graduationproject.zakerly.core.models.PushNotification;
import com.graduationproject.zakerly.core.models.Student;
import com.graduationproject.zakerly.core.network.firebase.FireBaseAuthenticationClient;
import com.graduationproject.zakerly.core.network.firebase.FirebaseDataBaseClient;
import com.graduationproject.zakerly.core.network.retrofit.RetrofitClient;
import com.graduationproject.zakerly.databinding.DialogSendRequestBinding;

import es.dmoral.toasty.Toasty;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AcceptNotificationDialog extends DialogFragment {
    private DialogSendRequestBinding binding;
    private AcceptNotificationDialogArgs args;
    private CompositeDisposable disposable;
    public static final String TAG = "SendRequestDialog";
    private Instructor currentUser;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        args = AcceptNotificationDialogArgs.fromBundle(requireArguments());
        disposable = new CompositeDisposable();
        setStyle(DialogFragment.STYLE_NORMAL, R.style.dialog);

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogSendRequestBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.verticalMargin = 10f;
            dialog.getWindow().setAttributes(params);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
            dialog.getWindow().setLayout(width, height);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        FirebaseDataBaseClient.getInstance().getCurrentUser().addOnSuccessListener(snapshot -> {
            currentUser = snapshot.getValue(Instructor.class);

            Glide.with(getContext())
                    .load(args.getNotification().getSenderImageUrl())
                    .centerCrop()
                    .error(R.drawable.no_user)
                    .into(binding.senderImage);

            Glide.with(getContext())
                    .load(currentUser.getUser().getProfileImg())
                    .centerCrop()
                    .error(R.drawable.no_user)
                    .into(binding.receiverImage);
        });

            binding.numberOfHoursInput.setText(Integer.toString(args.getNotification().getNeededHours()));
            binding.numberOfHoursInput.setEnabled(false);
            binding.numberOfHoursInput.setFocusable(false);
            binding.learningTopic.setText(args.getNotification().getTitle());
            binding.learningTopic.setEnabled(false);
            binding.learningTopic.setFocusable(false);
            binding.requestBody.setText(args.getNotification().getBody());
            binding.requestBody.setEnabled(false);
            binding.requestBody.setFocusable(false);


        if (args.getConnection().getRequestStatus() != RequestStatus.PENDING) {
            binding.send.setClickable(false);
            binding.cancel.setClickable(false);
            binding.send.setEnabled(false);
            binding.cancel.setEnabled(false);
        }
        binding.send.setText(getText(R.string.accept));
        binding.cancel.setText(getText(R.string.reject));
        binding.cancel.setOnClickListener(v -> {
            ConnectionModel c = args.getConnection();
            c.setRequestStatus(RequestStatus.ANSWERED);
            FirebaseDataBaseClient.getInstance().setConnection(c);
            dismiss();
        });
        binding.send.setOnClickListener(v -> {

            String notificationId = FirebaseDatabase.getInstance().getReference().push().getKey();
            NotificationData notificationData =
                    new NotificationData(
                            System.currentTimeMillis(),
                            notificationId,
                            binding.learningTopic.getText().toString(),
                            binding.requestBody.getText().toString(),
                            NotificationType.REQUEST,
                            currentUser.getUser().getFirstName() + " " + currentUser.getUser().getLastName(),
                            currentUser.getUser().getUID(),
                            args.getNotification().getSenderUid(),
                            Integer.parseInt(binding.numberOfHoursInput.getText().toString()));
            notificationData.setSenderImageUrl(currentUser.getUser().getProfileImg());

            FirebaseDataBaseClient.getInstance().getUserByUid(args.getNotification().getSenderUid()).addOnSuccessListener(snapshot -> {
                Student s = snapshot.getValue(Student.class);
                PushNotification pushNotification = new PushNotification(notificationData,
                        s.getUser().getNotificationToken());
                sendNotification(pushNotification);
                setUpConnection(notificationId);
            });
        });

    }

    private void setUpConnection(String notificationUid) {

        ConnectionModel c = args.getConnection();
        if (c == null) {
            c = new ConnectionModel(
                    FireBaseAuthenticationClient.getInstance().getCurrentUser().getUid(),
                    args.getNotification().getSenderUid(), RequestStatus.ANSWERED, true, notificationUid);
        }
        c.setLatestRequestUid(notificationUid);
        c.setRequestStatus(RequestStatus.ANSWERED);
        FirebaseDataBaseClient.getInstance().setConnection(c);
    }

    private void clearErrors() {
        binding.learningTopicContainer.setErrorEnabled(false);
        binding.numberOfHoursInputContainer.setErrorEnabled(false);
        binding.requestBodyContainer.setErrorEnabled(false);
    }

    private boolean valid() {
        if (binding.learningTopic.getText().toString().isEmpty()) {
            binding.learningTopicContainer.setErrorEnabled(true);
            binding.learningTopicContainer.setError(getString(R.string.this_field_cannot_be_empty));
            return false;
        }
        if (binding.numberOfHoursInput.getText().toString().isEmpty()) {
            binding.numberOfHoursInputContainer.setErrorEnabled(true);
            binding.numberOfHoursInputContainer.setError(getString(R.string.this_field_cannot_be_empty));
            return false;
        }
        if (binding.requestBody.getText().toString().isEmpty()) {
            binding.requestBodyContainer.setErrorEnabled(true);
            binding.requestBodyContainer.setError(getString(R.string.this_field_cannot_be_empty));
            return false;
        }
        return true;
    }

    private void sendNotification(PushNotification notification) {
        Disposable d = RetrofitClient.getApi()
                .postNotification(notification)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(responseBodyResponse -> {
                            if (responseBodyResponse.isSuccessful()) {
                                saveNotification(notification.getData(), args.getNotification().getSenderUid());
                            }
                        },
                        throwable -> Log.d(TAG, "sendNotification: error" + throwable.getMessage()));
        disposable.add(d);
    }

    private void saveNotification(NotificationData data, String uid) {
        FirebaseDataBaseClient.getInstance().addNotification(uid, data).addOnCompleteListener(task -> {
            Log.d(TAG, "onViewCreated: edited" + task.isSuccessful());
            Toasty.success(getContext(), getString(R.string.request_sent)).show();
            dismiss();

        });
    }

    @Override
    public void onPause() {
        super.onPause();
        disposable.dispose();
    }

}
