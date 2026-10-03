package com.example.jobmatch_frontend.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.jobmatch_frontend.R;
import com.example.jobmatch_frontend.models.DocumentListResponse;

import java.util.List;
import java.util.Locale;


public class AccountDocumentAdapter
        extends RecyclerView.Adapter<
        AccountDocumentAdapter.DocumentViewHolder> {


    public interface OnOcrClickListener {

        void onOcrClick(
                DocumentListResponse.DocumentItem document
        );
    }


    private final List<
            DocumentListResponse.DocumentItem
            > documents;

    private final OnOcrClickListener listener;


    public AccountDocumentAdapter(
            List<DocumentListResponse.DocumentItem> documents,
            OnOcrClickListener listener
    ) {

        this.documents = documents;

        this.listener = listener;
    }


    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_account_document,
                                parent,
                                false
                        );


        return new DocumentViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            @NonNull DocumentViewHolder holder,
            int position
    ) {

        DocumentListResponse.DocumentItem document =
                documents.get(position);


        holder.tvDocumentType.setText(
                capitalize(
                        document.getDocumentType()
                )
        );


        holder.tvDocumentName.setText(
                safeText(
                        document.getFileName()
                )
        );


        holder.tvDocumentStatus.setText(
                capitalize(
                        document.getUploadStatus()
                )
        );


        DocumentListResponse.OcrInfo ocr =
                document.getOcr();


        if (ocr != null) {

            Double confidence =
                    ocr.getConfidenceScore();


            if (confidence != null) {

                holder.tvOcrConfidence.setText(
                        String.format(
                                Locale.getDefault(),
                                "OCR Confidence: %.0f%%",
                                confidence
                        )
                );

            } else {

                holder.tvOcrConfidence.setText(
                        "OCR Confidence: Not available"
                );
            }


            holder.tvOcrIssuer.setText(
                    "Issuer: "
                            + safeText(
                            ocr.getExtractedIssuer()
                    )
            );


            holder.tvOcrPreview.setText(
                    safeText(
                            ocr.getExtractedText()
                    )
            );


            holder.btnViewOcr.setVisibility(
                    View.VISIBLE
            );


            holder.btnViewOcr.setOnClickListener(v -> {

                if (listener != null) {

                    listener.onOcrClick(
                            document
                    );
                }
            });

        } else {

            holder.tvOcrConfidence.setText(
                    "OCR result not available"
            );

            holder.tvOcrIssuer.setVisibility(
                    View.GONE
            );

            holder.tvOcrPreview.setVisibility(
                    View.GONE
            );

            holder.btnViewOcr.setVisibility(
                    View.GONE
            );
        }
    }


    @Override
    public int getItemCount() {

        return documents == null
                ? 0
                : documents.size();
    }


    private String safeText(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "Not available";
        }

        return value.trim();
    }


    private String capitalize(
            String value
    ) {

        if (
                value == null
                        ||
                        value.trim().isEmpty()
        ) {

            return "Unknown";
        }


        value = value.trim();


        return value.substring(0, 1)
                .toUpperCase(Locale.getDefault())
                + value.substring(1)
                .toLowerCase(Locale.getDefault());
    }


    static class DocumentViewHolder
            extends RecyclerView.ViewHolder {


        TextView tvDocumentType;
        TextView tvDocumentStatus;
        TextView tvDocumentName;
        TextView tvOcrConfidence;
        TextView tvOcrIssuer;
        TextView tvOcrPreview;

        Button btnViewOcr;


        public DocumentViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            tvDocumentType =
                    itemView.findViewById(
                            R.id.tvDocumentType
                    );

            tvDocumentStatus =
                    itemView.findViewById(
                            R.id.tvDocumentStatus
                    );

            tvDocumentName =
                    itemView.findViewById(
                            R.id.tvDocumentName
                    );

            tvOcrConfidence =
                    itemView.findViewById(
                            R.id.tvOcrConfidence
                    );

            tvOcrIssuer =
                    itemView.findViewById(
                            R.id.tvOcrIssuer
                    );

            tvOcrPreview =
                    itemView.findViewById(
                            R.id.tvOcrPreview
                    );

            btnViewOcr =
                    itemView.findViewById(
                            R.id.btnViewOcr
                    );
        }
    }
}