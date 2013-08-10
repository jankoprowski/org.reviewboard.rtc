package org.reviewboard.rtc.rb;

import java.util.Arrays;

import com.google.gson.annotations.SerializedName;


public class Draft {

	public String id;
	public String summary;
	public String description;
	public String branch;
	public String[] bugs_closed;
	public String testing_done;

	public String time_added;
	public String last_updated;

	public String repository;
	public String status;

	@SerializedName("public")
	public Boolean _public;

	public String[] target_groups  = new String[0];

	public Links links;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_public == null) ? 0 : _public.hashCode());
		result = prime * result + ((branch == null) ? 0 : branch.hashCode());
		result = prime * result + Arrays.hashCode(bugs_closed);
		result = prime * result
				+ ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((last_updated == null) ? 0 : last_updated.hashCode());
		result = prime * result
				+ ((repository == null) ? 0 : repository.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((summary == null) ? 0 : summary.hashCode());
		result = prime * result + Arrays.hashCode(target_groups);
		result = prime * result
				+ ((testing_done == null) ? 0 : testing_done.hashCode());
		result = prime * result
				+ ((time_added == null) ? 0 : time_added.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Draft other = (Draft) obj;
		if (_public == null) {
			if (other._public != null)
				return false;
		} else if (!_public.equals(other._public))
			return false;
		if (branch == null) {
			if (other.branch != null)
				return false;
		} else if (!branch.equals(other.branch))
			return false;
		if (!Arrays.equals(bugs_closed, other.bugs_closed))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (last_updated == null) {
			if (other.last_updated != null)
				return false;
		} else if (!last_updated.equals(other.last_updated))
			return false;
		if (repository == null) {
			if (other.repository != null)
				return false;
		} else if (!repository.equals(other.repository))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (summary == null) {
			if (other.summary != null)
				return false;
		} else if (!summary.equals(other.summary))
			return false;
		if (!Arrays.equals(target_groups, other.target_groups))
			return false;
		if (testing_done == null) {
			if (other.testing_done != null)
				return false;
		} else if (!testing_done.equals(other.testing_done))
			return false;
		if (time_added == null) {
			if (other.time_added != null)
				return false;
		} else if (!time_added.equals(other.time_added))
			return false;
		return true;
	}

}
