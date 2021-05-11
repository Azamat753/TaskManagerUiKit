package com.lawlett.taskmanageruikit.idea;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.idea.data.model.QuickModel;
import com.lawlett.taskmanageruikit.idea.recycler.IdeaAdapter;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;
import com.lawlett.taskmanageruikit.utils.preferences.IdeaViewPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static android.app.Activity.RESULT_OK;

public class IdeasFragment extends Fragment implements IdeaAdapter.ItemOnClickListener, IdeaAdapter.ShowImageInterface {
    private IdeaAdapter adapter;
    private List<QuickModel> list;
    private FloatingActionButton addQuickBtn;
    private int pos;
    private TextView firstText;
    private RecyclerView recyclerViewQuick;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;
    private ImageView btnChange;
    private ProgressBar progressBar;
    private final FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore.getInstance();
    private String collectionName;
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseUser user = mAuth.getCurrentUser();
    private String imageIdeaUri;
    private ImageView imageView;
    private String titleIdea;
    private QuickModel quickModel;
    private StorageReference storageReference;

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ideas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (user != null) {
            collectionName = "Идеи" + "-" + "(" + user.getDisplayName() + ")" + user.getUid();
        }
        initViews(view);
        initClickers();
        initAdapter();
        getRoomRecordsData();
        btnGridChange();
        initItemTouchHelper();
    }

    private void uploadImage(String imageUrl) {
        if (user != null) {
            storageReference = FirebaseStorage.getInstance().getReference().child(imageUrl);
            UploadTask task = storageReference.putFile(Uri.parse(imageUrl));
        }
    }

    private void getRoomRecordsData() {
        App.getDataBase().ideaDao().getAllLive().observe(this, quickModels -> {
            if (quickModels != null) {
                progressBar.setVisibility(View.GONE);
                list.clear();
                list.addAll(quickModels);
                firstText.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                if (quickModels.isEmpty()) {
                    firstText.setVisibility(View.VISIBLE);
                }
                if (quickModels.size() != 0) {
                    writeAllTaskFromRoomToFireStore();
                } else {
                    readDataFromFireStore();
                }
            }
        });
    }

    @SuppressLint("ResourceAsColor")
    private void initViews(View view) {
        list = new ArrayList<>();
        firstText = view.findViewById(R.id.quick_tv);
        recyclerViewQuick = view.findViewById(R.id.quick_recycler);
        btnChange = Objects.requireNonNull(getActivity()).findViewById(R.id.tool_btn_grid);
        addQuickBtn = view.findViewById(R.id.add_quick_btn);
        progressBar = view.findViewById(R.id.progress_bar);
        addQuickBtn.setColorFilter(Color.WHITE);
        addQuickBtn.setBackgroundColor(R.color.plus_background);
    }

    private void readDataFromFireStore() {
        if (user != null) {

            String createDateKey = "createData";
            String imageKey = "image";
            String titleKey = "title";
            progressBar.setVisibility(View.VISIBLE);
            db.collection(collectionName)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            progressBar.setVisibility(View.GONE);
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Random rnd = new Random();
                                int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
                                Map<String, Object> dataFromFireBase;
                                dataFromFireBase = document.getData();
                                String createDate = (String) dataFromFireBase.get(createDateKey);
                                String image = (String) dataFromFireBase.get(imageKey);
                                String title = (String) dataFromFireBase.get(titleKey);
                                QuickModel quickModel = new QuickModel(title, createDate, image, color);
                                App.getDataBase().ideaDao().insert(quickModel);
                            }
                        }
                    });
        }
    }

    private void writeAllTaskFromRoomToFireStore() {
        if (user != null) {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("ideaPreferences", Context.MODE_PRIVATE);
            Calendar calendar = Calendar.getInstance();
            String currentDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String dayFromPreference = sharedPreferences.getString(Constants.CURRENT_DAY, "");
            if (!currentDay.equals(dayFromPreference)) {
                for (int i = 0; i < list.size(); i++) {
                    FireStoreTools.deleteDataByFireStore(list.get(i).getTitle(), collectionName, db, null);
                }
                for (int i = 0; i < list.size(); i++) {
                    FireStoreTools.writeOrUpdateDataByFireStore(list.get(i).getTitle(), collectionName, db, list.get(i));
                }
                sharedPreferences.edit().clear().apply();
                sharedPreferences.edit().putString(Constants.CURRENT_DAY, currentDay).apply();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemClick(QuickModel quickModel) {
        showCreateIdeaDialog(quickModel);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void openGallery() {
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_PICK);
        final Intent chooserIntent = Intent.createChooser(galleryIntent, "Select Source");
        startActivityForResult(chooserIntent, Constants.YOUR_SELECT_PICTURE_REQUEST_CODE);
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);
        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable) drawable).getBitmap() != null;
        }
        return hasImage;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void showCreateIdeaDialog(QuickModel quickModel) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.create_idea_alert_layout, null);
        Dialog alertDialog = new Dialog(requireActivity());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        EditText editText = alertDialog.findViewById(R.id.editText_create_idea);
        imageView = alertDialog.findViewById(R.id.idea_image);
        editText.setText(quickModel.getTitle());
        imageView.setImageURI(Uri.parse(quickModel.getImage()));
        alertDialog.findViewById(R.id.apply_btn).setOnClickListener(v -> {
            if (editText.getText().toString().isEmpty()) {
                App.showToast(requireContext(), requireContext().getString(R.string.add_title));
            } else if (!hasImage(imageView)) {
                App.showToast(requireContext(), requireContext().getString(R.string.add_icon));
            } else {
                titleIdea = editText.getText().toString();
                quickModel.setTitle(titleIdea);
                if (imageIdeaUri != null) {
                    quickModel.setImage(imageIdeaUri);
                } else {
                    quickModel.setImage(quickModel.getImage());
                }
                FireStoreTools.writeOrUpdateDataByFireStore(titleIdea, collectionName, db, quickModel);
                App.getDataBase().ideaDao().update(quickModel);
                alertDialog.cancel();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                if (checkPermissionForReadExternalStorage()) {
                    openGallery();
                }else {
                    requestPermissionForReadExternalStorage();
                }
            }
        });
        alertDialog.show();
    }

    private void showCreateIdeaDialog() {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.create_idea_alert_layout, null);
        Dialog alertDialog = new Dialog(requireActivity());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        EditText editText = alertDialog.findViewById(R.id.editText_create_idea);
        imageView = alertDialog.findViewById(R.id.idea_image);
        alertDialog.findViewById(R.id.apply_btn).setOnClickListener(v -> {
            if (editText.getText().toString().isEmpty()) {
                Toast.makeText(requireContext(), requireContext().getString(R.string.add_title), Toast.LENGTH_SHORT).show();
            } else if (!hasImage(imageView)) {
                Toast.makeText(requireContext(), requireContext().getString(R.string.add_icon), Toast.LENGTH_SHORT).show();
            } else {
                titleIdea = editText.getText().toString();
                recordDataRoom();
                alertDialog.cancel();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                if (checkPermissionForReadExternalStorage()) {
                    openGallery();
                }else {
                    requestPermissionForReadExternalStorage();
                }
            }
        });
        alertDialog.show();
    }

    public void requestPermissionForReadExternalStorage() {
        try {
            ActivityCompat.requestPermissions((Activity) requireContext(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    01);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public boolean checkPermissionForReadExternalStorage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int result = requireContext().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    private void initClickers() {
        addQuickBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {
                showCreateIdeaDialog();
            }
        });
    }

    public void recordDataRoom() {
        Calendar c = Calendar.getInstance();
        final int year = c.get(Calendar.YEAR);
        String[] monthName = {getString(R.string.january), getString(R.string.february), getString(R.string.march), getString(R.string.april), getString(R.string.may), getString(R.string.june), getString(R.string.july),
                getString(R.string.august), getString(R.string.september), getString(R.string.october), getString(R.string.november), getString(R.string.december)};
        final String month = monthName[c.get(Calendar.MONTH)];
        String currentDate = new SimpleDateFormat("dd ", Locale.getDefault()).format(new Date());
        Random rnd = new Random();
        int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        quickModel = new QuickModel(titleIdea, currentDate + " " + month + " " + year, imageIdeaUri, color);
        uploadImage(imageIdeaUri);
        if (user!=null){
            FireStoreTools.writeOrUpdateDataByFireStore(titleIdea, collectionName, db, quickModel);
        }
        App.getDataBase().ideaDao().insert(quickModel);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.YOUR_SELECT_PICTURE_REQUEST_CODE) {
                Uri selectedImageUri = data == null ? null : data.getData();
                imageIdeaUri = selectedImageUri.toString();
                imageView.setImageURI(selectedImageUri);
            }
        }
    }

    private void initItemTouchHelper() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                if (fromPosition < toPosition) {
                    for (int i = fromPosition; i < toPosition; i++) {
                        Collections.swap(list, i, i + 1);

                        int order1 = (int) list.get(i).getId();
                        int order2 = (int) list.get(i + 1).getId();
                        list.get(i).setId(order2);
                        list.get(i + 1).setId(order1);
                    }
                } else {
                    for (int i = fromPosition; i > toPosition; i--) {
                        Collections.swap(list, i, i - 1);

                        int order1 = (int) list.get(i).getId();
                        int order2 = (int) list.get(i - 1).getId();
                        list.get(i).setId(order2);
                        list.get(i - 1).setId(order1);
                    }
                }
                adapter.notifyItemMoved(fromPosition, toPosition);
                return true;
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                App.getDataBase().ideaDao().updateWord(list);
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                PlannerDialog.showPlannerDialog(requireActivity(), getString(R.string.you_sure_delete), () -> {
                    pos = viewHolder.getAdapterPosition();
                    App.getDataBase().ideaDao().delete(list.get(pos));
                    Toast.makeText(getContext(), R.string.delete, Toast.LENGTH_SHORT).show();
                    if (user != null) {
                        FireStoreTools.deleteDataByFireStore(list.get(pos).getTitle(), collectionName, db, progressBar);
                    }
                });
                adapter.notifyDataSetChanged();
            }

            @SuppressLint("ResourceAsColor")
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                final int DIRECTION_RIGHT = 1;
                final int DIRECTION_LEFT = 0;
                pos = viewHolder.getAdapterPosition();
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && isCurrentlyActive) {
                    int direction = dX > 0 ? DIRECTION_RIGHT : DIRECTION_LEFT;
                    switch (direction) {
                        case DIRECTION_RIGHT:
                            View itemView = viewHolder.itemView;
                            ColorDrawable background = new ColorDrawable(list.get(pos).getColor());
                            background.setBounds(0, itemView.getTop(), (int) (itemView.getLeft() + dX), itemView.getBottom());
                            background.draw(c);
                            break;
                        case DIRECTION_LEFT:
                            View itemView2 = viewHolder.itemView;
                            ColorDrawable background2 = new ColorDrawable(list.get(pos).getColor());
                            background2.setBounds(itemView2.getRight(), itemView2.getBottom(), (int) (itemView2.getRight() + dX), itemView2.getTop());
                            background2.draw(c);
                            break;
                    }
                }
            }
        }).attachToRecyclerView(recyclerViewQuick);
    }

    private void initAdapter() {
        adapter = new IdeaAdapter(list, this, this, getContext());
        recyclerViewQuick.setAdapter(adapter);
        staggeredGridLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        recyclerViewQuick.setLayoutManager(staggeredGridLayoutManager);
        if (IdeaViewPreference.getInstance(getContext()).getView()) {
            staggeredGridLayoutManager.setSpanCount(2);
        } else {
            staggeredGridLayoutManager.setSpanCount(1);
        }
    }

    public void btnGridChange() {
        btnChange.setOnClickListener(v -> {
            if (!btnChange.isActivated()) {
                btnChange.setActivated(true);
                staggeredGridLayoutManager.setSpanCount(2);
                IdeaViewPreference.getInstance(getContext()).saveView(true);
            } else {
                btnChange.setActivated(false);
                staggeredGridLayoutManager.setSpanCount(1);
                IdeaViewPreference.getInstance(getContext()).saveView(false);
            }
            adapter.notifyDataSetChanged();
            adapter.isChange.setValue(false);
        });
    }

    public void showImageDialog(QuickModel model) {
        LayoutInflater inflater = LayoutInflater.from(requireActivity());
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.image_alert_layout, null);
        Dialog alertDialog = new Dialog(requireContext());
        alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(view);
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        ImageView imageView = alertDialog.findViewById(R.id.alert_image);
        imageView.setImageURI(Uri.parse(model.getImage()));
        alertDialog.show();
    }

    @Override
    public void show(QuickModel model) {
        showImageDialog(model);
    }
}
