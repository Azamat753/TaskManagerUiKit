package com.lawlett.taskmanageruikit.finance;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lawlett.taskmanageruikit.R;
import com.lawlett.taskmanageruikit.finance.adapter.DialogImageAdapter;
import com.lawlett.taskmanageruikit.finance.adapter.FinanceMainAdapter;
import com.lawlett.taskmanageruikit.finance.adapter.FrequentSpendingAdapter;
import com.lawlett.taskmanageruikit.finance.model.FrequentSpendingModel;
import com.lawlett.taskmanageruikit.finance.model.SpendingModel;
import com.lawlett.taskmanageruikit.utils.App;
import com.lawlett.taskmanageruikit.utils.Constants;
import com.lawlett.taskmanageruikit.utils.DialogHelper;
import com.lawlett.taskmanageruikit.utils.FireStoreTools;
import com.lawlett.taskmanageruikit.utils.OkButtonClickListener;
import com.lawlett.taskmanageruikit.utils.PlannerDialog;
import com.lawlett.taskmanageruikit.utils.financeDialog.AdviceDialog;
import com.lawlett.taskmanageruikit.utils.financeDialog.AlertDialogFragment;
import com.lawlett.taskmanageruikit.utils.financeDialog.AlertDialogFragmentQt2;
import com.lawlett.taskmanageruikit.utils.financeDialog.FrequentSpendingDialog;
import com.lawlett.taskmanageruikit.utils.financeDialog.HelpDialogFragment;
import com.lawlett.taskmanageruikit.utils.financeDialog.SpendingDialogFragment;
import com.lawlett.taskmanageruikit.utils.preferences.FinancePreference;
import com.lawlett.taskmanageruikit.utils.preferences.LanguagePreference;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class FinanceFragment extends Fragment implements OkButtonClickListener, DialogImageAdapter.IIdeaOnClickListener {
    private TextView tvBalanceAmount, tvIncomeAmount, tvSavingsAmount, tvSpendingAmount;
    private ImageView ivAddSavings, ivAddIncome, ivAddSpending, ivAddFrequentSpending;
    private List<FrequentSpendingModel> list = new ArrayList<>();
    private FrequentSpendingAdapter adapterFS;
    private FinancePreference preference;
    private AlertDialogFragment alertDialog;
    private String collectionName;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseUser user = mAuth.getCurrentUser();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preference = new FinancePreference(requireContext());
        this.list = App.getDataBase().frequentSpendingDao().getAllList();
        adapterFS = new FrequentSpendingAdapter(list, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loadLocale();
        return inflater.inflate(R.layout.fragment_finance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (user!=null){
            collectionName = "Финансы" + "-" + "(" + user.getDisplayName() + ")" + user.getUid();
        }
        initViews(view);
        setPreferences();
        setClickers();
        checkLiveData();
//        readFromFireBaseToPreferences();
//        writeAllFromRoomToFireStore();
    }
    private void loadLocale() {
        String language = LanguagePreference.getInstance(requireContext()).getLanguage();
        setLocale(language);
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        requireActivity().getBaseContext().getResources().updateConfiguration(config, requireActivity().getBaseContext().getResources().getDisplayMetrics());
        LanguagePreference.getInstance(requireContext()).saveLanguage(lang);
    }


    private void writeAllFromRoomToFireStore() {
        if (user != null) {
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("spendingPreferences", Context.MODE_PRIVATE);
            String balance = preference.getBalance();
            String income = preference.getIncomeAmount();
            String saving = preference.getSavingsAmount();
            String spending = preference.getSpendingAmount();
            Calendar calendar = Calendar.getInstance();
            String currentDay = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String dayFromPreference = sharedPreferences.getString(Constants.CURRENT_DAY, "");
            if (!currentDay.equals(dayFromPreference)) {
                HashMap<String, String> map = new HashMap<>();
                map.put("balance", balance);
                map.put("income", income);
                map.put("saving", saving);
                map.put("spending", spending);
                FireStoreTools.writeOrUpdateDataByFireStore(getString(R.string.finance), collectionName, db, map);
                sharedPreferences.edit().clear().apply();
                sharedPreferences.edit().putString("currentDay", currentDay).apply();
            }
        }
    }

    private void checkLiveData() {
        App.getDataBase().frequentSpendingDao().getAll().observe(getViewLifecycleOwner(), list -> {
            this.list = list;
            adapterFS.update(list);
        });
    }

    private void readFromFireBaseToPreferences() {
        if (user != null) {
            if (preference.getBalance().equals("0") && preference.getIncomeAmount().equals("0") &&
                    preference.getSavingsAmount().equals("0") && preference.getSpendingAmount().equals("0")) {
                String balance = "balance";
                String income = "income";
                String saving = "saving";
                String spending = "spending";
                db.collection(collectionName).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                Map<String, Object> dataFromFireBase;
                                dataFromFireBase = document.getData();
                                String balanceFB = (String) dataFromFireBase.get(balance);
                                String incomeFB = (String) dataFromFireBase.get(income);
                                String savingFB = (String) dataFromFireBase.get(saving);
                                String spendingFB = (String) dataFromFireBase.get(spending);

                                preference.setBalance(balanceFB);
                                preference.setIncomeAmount(incomeFB);
                                preference.setSavingsAmount(savingFB);
                                preference.setSpendingAmount(spendingFB);

                                tvBalanceAmount.setText(balanceFB);
                                tvIncomeAmount.setText(incomeFB);
                                tvSavingsAmount.setText(savingFB);
                                tvSpendingAmount.setText(spendingFB);
                            }
                        }
                    }
                });
            }
        }
    }

    private void initViews(View view) {
        tvBalanceAmount = view.findViewById(R.id.balance_amount_tv);
        tvIncomeAmount = view.findViewById(R.id.income_amount_tv);
        tvSavingsAmount = view.findViewById(R.id.savings_amount_tv);
        tvSpendingAmount = view.findViewById(R.id.spending_amount_tv);
        ivAddSavings = view.findViewById(R.id.add_savings_iv);
        ivAddIncome = view.findViewById(R.id.income_plus_iv);
        ivAddSpending = view.findViewById(R.id.spending_minus_iv);
        ivAddFrequentSpending = view.findViewById(R.id.frequent_spending_add_iv);
        RecyclerView frequentSpendRecycler = view.findViewById(R.id.frequent_spending_recycler);
        frequentSpendRecycler.setAdapter(adapterFS);
        new ItemTouchHelper(simpleCallback).attachToRecyclerView(frequentSpendRecycler);

        RecyclerView topRecycler = view.findViewById(R.id.finance_recycler);
        FinanceMainAdapter adapter = new FinanceMainAdapter(requireContext(),position -> {
            switch (position) {
                case 0:
                    new AdviceDialog().show(getChildFragmentManager(), "advice dialog");
                    break;
                case 1:
                    new SpendingDialogFragment().show(getChildFragmentManager(), "spending dialog");
                    new SpendingDialogFragment().show(getChildFragmentManager(), "spending dialog");
                    break;
                case 2:
                    new HelpDialogFragment().show(getChildFragmentManager(), "help dialog");
                    break;
            }
        });
        topRecycler.setAdapter(adapter);
    }

    private void setPreferences() {
        tvBalanceAmount.setText(preference.getBalance());
        tvSavingsAmount.setText(preference.getSavingsAmount());
        tvIncomeAmount.setText(preference.getIncomeAmount());
        tvSpendingAmount.setText(preference.getSpendingAmount());
    }

    private void setClickers() {
        ivAddFrequentSpending.setOnClickListener(v ->
                new FrequentSpendingDialog(this).show(requireActivity().getSupportFragmentManager(), "dialog"));
        ivAddSavings.setOnClickListener(v -> {
            alertDialog = new AlertDialogFragment(this);
            alertDialog.show(requireActivity().getSupportFragmentManager(), "savings");
        });
        ivAddIncome.setOnClickListener(v -> {
            alertDialog = new AlertDialogFragment(this);
            alertDialog.show(requireActivity().getSupportFragmentManager(), "income");
        });
        ivAddSpending.setOnClickListener(v ->
                new AlertDialogFragmentQt2(this).show(requireActivity().getSupportFragmentManager(), "alertQ2"));

        ivAddSavings.setOnLongClickListener(v -> {
            new DialogHelper().myDialog2(requireContext(), getString(R.string.attention),
                    getString(R.string.you_sure_delete), getString(R.string.yes), getString(R.string.no), () -> {
                        preference.setSavingsAmount("0");
                        tvSavingsAmount.setText(preference.getSavingsAmount());
                    });
            return true;
        });
    }


    private String getResult(String from, String to, String operation) {
        if (operation.equals("+"))
            return String.valueOf(Double.parseDouble(from) + Double.parseDouble(to));
        else return String.valueOf(Double.parseDouble(from) - Double.parseDouble(to));
    }

    @Override
    public void onClick(String amount) {
        String amountSum;
        assert alertDialog.getTag() != null;
        if (alertDialog.getTag().equals("savings")) {
            //savings
            amountSum = getResult(preference.getSavingsAmount(), amount, "+");
            preference.setSavingsAmount(amountSum);
            tvSavingsAmount.setText(amountSum);
        } else if ( // income
                alertDialog.getTag().equals("income")) {
            amountSum = getResult(preference.getBalance(), amount, "+");
            preference.setBalance(amountSum);
            preference.setIncomeAmount(amount);
            tvBalanceAmount.setText(preference.getBalance());
            tvIncomeAmount.setText(amount);
        } else { // frequent spending
            if (checkBalance(Double.parseDouble(amount))) {
                preference.setBalance(getResult(preference.getBalance(), amount, "-"));
                tvBalanceAmount.setText(preference.getBalance());
                amountSum = getResult(amount, preference.getSpendingAmount(), "+");
                preference.setSpendingAmount(amountSum);
                tvSpendingAmount.setText(amountSum);
                FrequentSpendingModel model = list.get(position);
                App.getDataBase().spendingDao().insert(new SpendingModel(amount, model.getName(), System.currentTimeMillis()));
                model.setAmount(getResult(amount, model.getAmount(), "+"));
                App.getDataBase().frequentSpendingDao().update(model);
            } else
                new DialogHelper().myDialog2(requireContext(), getString(R.string.error), getString(R.string.not_enough_balanсe),
                        "", getString(R.string.apply), () -> {
                        });
        }
    }

    public boolean checkBalance(Double amount) {
        return Double.parseDouble(preference.getBalance()) - amount >= 0;
    }

    @Override
    public void onClick(SpendingModel spendingModel) {
        if (checkBalance(Double.parseDouble(spendingModel.getAmount()))) {
            String amountSum = getResult(spendingModel.getAmount(), preference.getSpendingAmount(), "+");
            preference.setSpendingAmount(amountSum);
            preference.setBalance(getResult(preference.getBalance(), spendingModel.getAmount(), "-"));
            tvBalanceAmount.setText(preference.getBalance());
            tvSpendingAmount.setText(amountSum);
            App.getDataBase().spendingDao().insert(spendingModel);
        } else
            new DialogHelper().myDialog2(requireContext(), getString(R.string.error), getString(R.string.not_enough_balanсe),
                    "", getString(R.string.apply), () -> {
                    });
    }

    private int position;

    @Override
    public void onItemClick(int position) {
        this.position = position;
        alertDialog = new AlertDialogFragment(this);
        alertDialog.show(requireActivity().getSupportFragmentManager(), "frequent_spend");
    }

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |
            ItemTouchHelper.DOWN, ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            int dragPosition = viewHolder.getAdapterPosition();
            int targetPosition = target.getAdapterPosition();
            Collections.swap(adapterFS.list, dragPosition, targetPosition);
            adapterFS.notifyItemMoved(dragPosition, targetPosition);
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            PlannerDialog.showPlannerDialog(requireActivity(),getString(R.string.attention) ,getString(R.string.task_dialog_message), () -> App.getDataBase().frequentSpendingDao().delete(list.get(viewHolder.getAdapterPosition())));
            adapterFS.notifyDataSetChanged();
        }
    };
}