package org.reviewboard.rtc.rb;

import java.util.Arrays;



public class ReviewRequest extends Draft {	

	public String[] target_people;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(target_people);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReviewRequest other = (ReviewRequest) obj;
		if (!Arrays.equals(target_people, other.target_people))
			return false;
		return true;
	}


}
