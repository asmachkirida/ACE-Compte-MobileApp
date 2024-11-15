package com.example.myapplication.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Compte;
import java.util.List;

public class CompteAdapter extends RecyclerView.Adapter<CompteAdapter.CompteViewHolder> {
    private List<Compte> compteList;
    private OnDeleteClickListener onDeleteClickListener;

    public interface OnDeleteClickListener {
        void onDeleteClick(Compte compte);
    }

    public CompteAdapter(List<Compte> compteList, OnDeleteClickListener onDeleteClickListener) {
        this.compteList = compteList;
        this.onDeleteClickListener = onDeleteClickListener;
    }

    @NonNull
    @Override
    public CompteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_compte, parent, false);
        return new CompteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompteViewHolder holder, int position) {
        Compte compte = compteList.get(position);
        holder.textViewId.setText("ID: " + compte.getId());
        holder.textViewSolde.setText("Solde: " + compte.getSolde());
        holder.textViewType.setText("Type: " + compte.getType());
        holder.textViewDate.setText("Date de création: " + compte.getDateCreation());

        // Handle delete button click
        holder.buttonDelete.setOnClickListener(view -> {
            onDeleteClickListener.onDeleteClick(compte);
        });
    }

    @Override
    public int getItemCount() {
        return compteList.size();
    }

    public static class CompteViewHolder extends RecyclerView.ViewHolder {
        TextView textViewId, textViewSolde, textViewType, textViewDate;
        ImageButton buttonDelete;

        public CompteViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewId = itemView.findViewById(R.id.textViewId);
            textViewSolde = itemView.findViewById(R.id.textViewSolde);
            textViewType = itemView.findViewById(R.id.textViewType);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}