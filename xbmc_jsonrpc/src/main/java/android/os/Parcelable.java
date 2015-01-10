package android.os;


public interface Parcelable {

	public class Creator<T> {

		public T createFromParcel(Parcel parcel) {
			// TODO Auto-generated method stub
			return null;
		}

		public T[] newArray(int n) {
			// TODO Auto-generated method stub
			return null;
		}

	}

	void writeToParcel(Parcel parcel, int flags);

	int describeContents();

}
