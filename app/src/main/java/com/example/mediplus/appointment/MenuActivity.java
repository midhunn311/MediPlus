package com.example.mediplus.appointment;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import com.example.mediplus.Doctor.Doctor_login;
import com.example.mediplus.R;
import com.example.mediplus.appointment.models.Appointment;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Calendar;

import cn.pedant.SweetAlert.SweetAlertDialog;
import de.hdodenhof.circleimageview.CircleImageView;

public class MenuActivity extends AppCompatActivity {
    TextView fullName;
    CircleImageView profilePicture;
    SharedPreferences sp;
    DatabaseReference databaseReference;
    FirebaseUser user;
    String uid;
    NotificationCompat.Builder builder;
    String patientFullName, patientCin, todaysDate;
    int numberOfAppointments;
    static int token = 0;
    String[] numbers = {"one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten"};

    public static void setToken(int number)
    {
        token = number;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
        numberOfAppointments = 0;
        sp = getSharedPreferences("login", MODE_PRIVATE);
        user = FirebaseAuth.getInstance().getCurrentUser();
        fullName = findViewById(R.id.fullName1);
        profilePicture = findViewById(R.id.profile_image);
        uid = user.getUid();
        Calendar c = Calendar.getInstance();
        todaysDate = DateFormat.getDateInstance(DateFormat.SHORT).format(c.getTime());


        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Appointments");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    Appointment appointment = data.getValue(Appointment.class);
                    if(appointment!=null && appointment.getEmailPatient()!=null && FirebaseAuth.getInstance().getCurrentUser().getEmail()!=null){
                        if(appointment.getEmailPatient().equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()) &&
                                appointment.getStatus().equals("Accepted") &&
                                appointment.getDate().equals(todaysDate)
                        ){
                            numberOfAppointments++;
                        }
                    }
                }

                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("YOUR_CHANNEL_ID",
                            "YOUR_CHANNEL_NAME",
                            NotificationManager.IMPORTANCE_DEFAULT);
                    channel.setDescription("YOUR_NOTIFICATION_CHANNEL_DESCRIPTION");
                    mNotificationManager.createNotificationChannel(channel);
                }


                if (numberOfAppointments == 1) {
                    builder = new NotificationCompat.Builder(MenuActivity.this,"YOUR_CHANNEL_ID")
                            .setSmallIcon(R.drawable.ic_heart_beats)
                            .setContentTitle("Daily appointments")
                            .setContentText("You have one appointment today")
                            .setAutoCancel(true)
                            .setColor(Color.parseColor("#da0384"))
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);
                }
                if (numberOfAppointments > 1) {
                    builder = new NotificationCompat.Builder(MenuActivity.this, "YOUR_CHANNEL_ID")
                            .setSmallIcon(R.drawable.ic_heart_beats)
                            .setContentTitle("Daily appointments")
                            .setContentText("You have " + numbers[numberOfAppointments - 1] + " appointments today")
                            .setAutoCancel(true)
                            .setColor(Color.parseColor("#da0384"))
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                }
                if (numberOfAppointments == 0) {
                    builder = new NotificationCompat.Builder(MenuActivity.this, "YOUR_CHANNEL_ID")
                            .setSmallIcon(R.drawable.ic_heart_beats)
                            .setContentTitle("Daily appointments")
                            .setContentText("You have no appointments today")
                            .setAutoCancel(true)
                            .setColor(Color.parseColor("#da0384"))
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setPriority(NotificationCompat.PRIORITY_HIGH);

                }
                if(token == 0) {
                    Intent intent = new Intent(MenuActivity.this, AppointmentsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    PendingIntent pendingIntent = PendingIntent.getActivity(MenuActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    builder.setContentIntent(pendingIntent);

                    NotificationManager notificationManager = (NotificationManager) getSystemService(
                            Context.NOTIFICATION_SERVICE
                    );
                    notificationManager.notify(0, builder.build());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        databaseReference = FirebaseDatabase.getInstance().getReference("Patients");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                patientFullName = dataSnapshot.child(uid).child("fullName").getValue(String.class) ;
                fullName.setText(patientFullName);
               // cin.setText(patientCin);
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                StorageReference profileRef = storageReference.child("Profile_pictures").child(FirebaseAuth.getInstance().getCurrentUser().getEmail() + ".jpg");
                profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profilePicture);
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void searchDoctor(View view) {
        Intent intent = new Intent(MenuActivity.this, SearchDoctorSpecialityActivity.class);
        startActivity(intent);
    }

    private void goToUrl (String url) {
        Uri uriUrl = Uri.parse(url);
        Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
        startActivity(launchBrowser);
    }

    public void goToSo (View view) {
        goToUrl ( "http://silvercityhospital.com");
    }

    public void Appointments(View view) {
        Intent intent = new Intent(MenuActivity.this, AppointmentsActivity.class);
        startActivity(intent);
    }

    public void profileInfo(View view) {
        Intent intent = new Intent(MenuActivity.this, PatientProfileInformations.class);
        startActivity(intent);
    }


    public void Feedback(View view){
        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this);
        sweetAlertDialog.setTitleText("Will be available soon");
        sweetAlertDialog.setContentText("We are constantly developing,this feature will develop soon. Thank you!");
        sweetAlertDialog.show();
        Button button = sweetAlertDialog.getButton(SweetAlertDialog.BUTTON_CONFIRM);
        button.setBackgroundColor(Color.parseColor("#da0384"));
    }

    public void openMedicalFolder(View view) {
        Intent intent = new Intent(MenuActivity.this, MedicalFolderActivity.class);
        startActivity(intent);
    }

    public void logOut(View view) {
        sp.edit().putBoolean("loggedPatient", false).apply();
        FirebaseAuth.getInstance().signOut();
        finish();
        startActivity(new Intent(MenuActivity.this, Doctor_login.class));

    }

    @Override
    public void onBackPressed() {

        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.setConfirmText("Yes");
        dialog.setCancelText("No");
        dialog.setContentText("Are you sure want to close SilverCity application ?");
        dialog.setTitleText("Close application");
        dialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                finishAffinity();
                System.exit(0);
            }
        });
        dialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                sweetAlertDialog.cancel();
            }
        });

        dialog.show();
    }

    public void myDoctors(View view) {
        Intent intent = new Intent(MenuActivity.this, MyDoctorsActivity.class);
        startActivity(intent);
    }
}

