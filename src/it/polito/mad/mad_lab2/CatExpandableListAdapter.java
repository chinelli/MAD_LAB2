package it.polito.mad.mad_lab2;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CatExpandableListAdapter extends BaseExpandableListAdapter{
	private Context ctx;
	private List<String> listDataHeader;
	private HashMap<String, List<CatExpandableListItem>> listDataChild;
	
	public CatExpandableListAdapter(Context ctx, List<String> listDataHeader,
			HashMap<String, List<CatExpandableListItem>> listDataChild) {
		this.ctx = ctx;
		this.listDataHeader = listDataHeader;
		this.listDataChild = listDataChild;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return listDataChild.get(listDataHeader.get(groupPosition)).get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
		final CatExpandableListItem childText = (CatExpandableListItem) getChild(groupPosition, childPosition);
		 
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandable_list_item, null);
        }
 
        TextView tvListItem = (TextView) convertView.findViewById(R.id.tvListItem);
        tvListItem.setText(childText.getName());
        
        return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return listDataChild.get(listDataHeader.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return listDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return listDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
		String headerTitle = (String) getGroup(groupPosition);
		
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.expandable_list_header, null);
        }
        
        ImageView tag_img = (ImageView) convertView.findViewById(R.id.tag_img);
        TextView tvListHeader = (TextView) convertView.findViewById(R.id.tvListHeader);
        
        if(isExpanded)
        	tag_img.setImageResource(R.drawable.group_up);
        else
        	tag_img.setImageResource(R.drawable.group_down);
        
        tvListHeader.setTypeface(null, Typeface.BOLD);
        tvListHeader.setText(headerTitle);
 
        return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
