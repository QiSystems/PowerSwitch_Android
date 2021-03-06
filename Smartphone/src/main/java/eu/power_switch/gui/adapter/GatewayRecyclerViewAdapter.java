/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.gui.adapter;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import eu.power_switch.R;
import eu.power_switch.database.handler.DatabaseHandler;
import eu.power_switch.gui.IconicsHelper;
import eu.power_switch.gui.StatusMessageHandler;
import eu.power_switch.obj.gateway.Gateway;
import eu.power_switch.shared.log.Log;

/**
 * * Adapter to visualize Gateway items in RecyclerView
 * <p/>
 * Created by Markus on 27.07.2015.
 */
public class GatewayRecyclerViewAdapter extends RecyclerView.Adapter<GatewayRecyclerViewAdapter.ViewHolder> {
    private ArrayList<Gateway> gateways;
    private Context context;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public GatewayRecyclerViewAdapter(Context context, ArrayList<Gateway> gateways) {
        this.gateways = gateways;
        this.context = context;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    public GatewayRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.list_item_gateway, parent, false);
        return new GatewayRecyclerViewAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GatewayRecyclerViewAdapter.ViewHolder holder, int position) {
        final Gateway gateway = gateways.get(holder.getAdapterPosition());

        holder.gatewaySwitchStatus.setChecked(gateway.isActive());
        holder.gatewaySwitchStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // check if user pressed the button
                if (buttonView.isPressed()) {
                    try {
                        if (isChecked) {
                            DatabaseHandler.enableGateway(gateway.getId());
                        } else {
                            DatabaseHandler.disableGateway(gateway.getId());
                        }
                        gateway.setActive(isChecked);
                    } catch (Exception e) {
                        Log.e(e);
                        StatusMessageHandler.showInfoMessage(context, R.string.error_enabling_gateway, 5000);
                    }
                }
            }
        });

        boolean isAssociatedWithApartment = true;
        try {
            isAssociatedWithApartment = DatabaseHandler.isAssociatedWithAnyApartment(gateway);
        } catch (Exception e) {
            Log.e(e);
        }

        holder.attention.setImageDrawable(IconicsHelper.getAttentionIcon(context));
        holder.attention.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle(R.string.attention)
                        .setMessage(R.string.gateway_not_associated_with_any_apartment)
                        .setNeutralButton(R.string.close, null)
                        .show();
            }
        });
        if (!isAssociatedWithApartment) {
            holder.attention.setVisibility(View.VISIBLE);
        } else {
            holder.attention.setVisibility(View.GONE);
        }

        holder.name.setText(gateway.getName());
        holder.model.setText(gateway.getModel());

        if (gateway.hasValidLocalAddress()) {
            holder.layoutLocalAddress.setVisibility(View.VISIBLE);
        } else {
            holder.layoutLocalAddress.setVisibility(View.GONE);
        }
        holder.localAddress.setText(gateway.getLocalHost() + ":" + String.valueOf(gateway.getLocalPort()));
        if (gateway.hasValidWanAddress()) {
            holder.layoutWanAddress.setVisibility(View.VISIBLE);
        } else {
            holder.layoutWanAddress.setVisibility(View.GONE);
        }
        holder.wanAddress.setText(gateway.getWanHost() + ":" + String.valueOf(gateway.getWanPort()));

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            holder.footer.setVisibility(View.VISIBLE);
        } else {
            holder.footer.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return gateways.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View itemView, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView attention;
        public TextView name;
        public TextView model;
        public LinearLayout layoutLocalAddress;
        public TextView localAddress;
        public LinearLayout layoutWanAddress;
        public TextView wanAddress;
        public android.support.v7.widget.SwitchCompat gatewaySwitchStatus;
        public LinearLayout footer;

        public ViewHolder(final View itemView) {
            super(itemView);
            attention = (ImageView) itemView.findViewById(R.id.imageView_attention);
            name = (TextView) itemView.findViewById(R.id.txt_gateway_name);
            model = (TextView) itemView.findViewById(R.id.txt_gateway_model);
            layoutLocalAddress = (LinearLayout) itemView.findViewById(R.id.layout_local_address);
            localAddress = (TextView) itemView.findViewById(R.id.txt_gateway_local_address);
            layoutWanAddress = (LinearLayout) itemView.findViewById(R.id.layout_wan_address);
            wanAddress = (TextView) itemView.findViewById(R.id.txt_gateway_wan_address);

            gatewaySwitchStatus = (android.support.v7.widget.SwitchCompat) itemView.findViewById(R.id.switch_gateway_status);
            footer = (LinearLayout) itemView.findViewById(R.id.list_footer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(itemView, getLayoutPosition());
                    }
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (onItemLongClickListener != null) {
                        onItemLongClickListener.onItemLongClick(itemView, getLayoutPosition());
                    }
                    return true;
                }
            });
        }
    }
}
