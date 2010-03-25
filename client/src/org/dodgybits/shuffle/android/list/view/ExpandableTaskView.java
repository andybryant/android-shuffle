/*
 * Copyright (C) 2009 Android Shuffle Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dodgybits.shuffle.android.list.view;

import org.dodgybits.android.shuffle.R;
import org.dodgybits.shuffle.android.core.model.Context;
import org.dodgybits.shuffle.android.core.model.Project;
import org.dodgybits.shuffle.android.core.model.persistence.EntityCache;

import android.widget.RelativeLayout;

public class ExpandableTaskView extends TaskView {

	public ExpandableTaskView(
	        android.content.Context androidContext,
            EntityCache<Context> contextCache,
            EntityCache<Project> projectCache) {
		super(androidContext, contextCache, projectCache);
		
		RelativeLayout layout = (RelativeLayout) findViewById(R.id.relLayout);
		// 36 is the current value of ?android:attr/expandableListPreferredItemPaddingLeft
		// TODO get that value programatically
		layout.setPadding(36, 0, 7, 0);
	}
	
}