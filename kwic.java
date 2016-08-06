

package kwic.ms;


import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class KWIC {


	private char[] chars_;


	private int[] line_index_;


	private int[][] circular_shifts_;


	private int[][] alphabetized_;

	// Added by Rye Yao +++++++++++++++++++++++++++++++++++
	private char[] shifts_chars_;
	private int[] shifts_index_;
	private int[] shifts_lines_len_;

	--------------------------------------------------------------------
	

	public void input(String file) {

		// initialize line index
		line_index_ = new int[32];

		// initialize chars array
		chars_ = new char[2048];

		// count of valid characters in the buffer
		int char_count = 0;

		// count of parsed lines
		int line_count = 0;

		// the last read character
		int c;

		// new line flag
		boolean is_new_line = true;

		// new word flag
		boolean is_new_word = false;

		// line started flag
		boolean is_line_started = false;

		try {

			// open the file for reading
			InputStream in = new FileInputStream(file);

			// read characters until EOF is reached
			c = in.read();
			while (c != -1) {

				// parse the character
				switch ((byte) c) {
				case '\n':
					is_new_line = true;
					break;
				case ' ':
					is_new_word = true;
					break;
				case '\t':
					is_new_word = true;
					break;
				case '\r':
					break;
				default:

					// if this is a new line we need to update the line index
					if (is_new_line) {

						
						if (line_count == line_index_.length) {
							int[] new_index = new int[line_count + 32];
							System.arraycopy(line_index_, 0, new_index, 0,
									line_count);
							line_index_ = new_index;
						}

						// we assign the index in the original char array as the
						// start of the new line and increment the line counter
						line_index_[line_count] = char_count;
						line_count++;

						// we handled the new line, so we set the new line flag
						// to false
						is_new_line = false;

						// we set line started flag
						is_line_started = false;
					}

				
					if (is_new_word) {

						
						if (is_line_started) {

							if (char_count == chars_.length) {
								char[] new_chars = new char[char_count + 2048];
								System.arraycopy(chars_, 0, new_chars, 0,
										char_count);
								chars_ = new_chars;
							}

							// we add the word delimiter in the chars array
							chars_[char_count] = ' ';
							char_count++;
						}

						// we handled the new word, so we set the new word flag
						// to false
						is_new_word = false;
					}

					if (char_count == chars_.length) {
						char[] new_chars = new char[char_count + 2048];
						System.arraycopy(chars_, 0, new_chars, 0, char_count);
						chars_ = new_chars;
					}

					// add the character
					chars_[char_count] = (char) c;
					char_count++;

					// since we added at least one character we already
					// started the new line
					is_line_started = true;

					break;
				}

				// read the next character
				c = in.read();
			}

			// set the size of the index array to the real number of lines
			if (line_count != line_index_.length) {
				int[] new_index = new int[line_count];
				System.arraycopy(line_index_, 0, new_index, 0, line_count);
				line_index_ = new_index;
			}

			// set the size of the chars array to the real number of characters
			if (char_count != chars_.length) {
				char[] new_chars = new char[char_count];
				System.arraycopy(chars_, 0, new_chars, 0, char_count);
				chars_ = new_chars;
			}

		} catch (FileNotFoundException exc) {

			// handle the exception if the file could not be found
			exc.printStackTrace();
			System.err.println("KWIC Error: Could not open " + file + "file.");
			System.exit(1);

		} catch (IOException exc) {

			// handle other system I/O exception
			exc.printStackTrace();
			System.err.println("KWIC Error: Could not read " + file + "file.");
			System.exit(1);

		}
	}


	public void circularShift() {


		circular_shifts_ = new int[2][256];

		
		int shift_count = 0;

	
		for (int i = 0; i < line_index_.length; i++) {

			// end index of the i-th line
			int line_end = 0;

			
			if (i == (line_index_.length - 1))
				line_end = chars_.length;

			
			else
				line_end = line_index_[i + 1];

			
			for (int j = line_index_[i]; j < line_end; j++) {

				
				if ((chars_[j] == ' ') || (j == line_index_[i])) {

					
					if (shift_count == circular_shifts_[0].length) {

						// copy the line number row
						int[] tmp = new int[shift_count + 256];
						System.arraycopy(circular_shifts_[0], 0, tmp, 0,
								shift_count);
						circular_shifts_[0] = tmp;

						// copy the indices row
						tmp = new int[shift_count + 256];
						System.arraycopy(circular_shifts_[1], 0, tmp, 0,
								shift_count);
						circular_shifts_[1] = tmp;
					}

					// set the original line number
					circular_shifts_[0][shift_count] = i;
					// set the starting index of this circular shift
					circular_shifts_[1][shift_count] = (j == line_index_[i]) ? j
							: j + 1;

					// increment the shift count
					shift_count++;
				}

			}
		}

		// set the columns size of shift matrix to the real number of shifts
		if (shift_count != circular_shifts_[0].length) {

			// copy the line number row
			int[] tmp = new int[shift_count];
			System.arraycopy(circular_shifts_[0], 0, tmp, 0, shift_count);
			circular_shifts_[0] = tmp;

			// copy the indices row
			tmp = new int[shift_count];
			System.arraycopy(circular_shifts_[1], 0, tmp, 0, shift_count);
			circular_shifts_[1] = tmp;
		}

	}

	

	public void alphabetizing() {

		
		alphabetized_ = new int[2][circular_shifts_[0].length];

		int alphabetized_count = 0;

		
		int low = 0;
		int high = 0;
		int mid = 0;

		// process the circular shifts
		for (int i = 0; i < alphabetized_[0].length; i++) {

			// the index of original line
			int line_number = circular_shifts_[0][i];

			int shift_start = circular_shifts_[1][i];

			
			int line_start = line_index_[line_number];

			// the end of the original line
			int line_end = 0;

			
			if (line_number == (line_index_.length - 1))
				line_end = chars_.length;

			
			else
				line_end = line_index_[line_number + 1];

			char[] current_shift = new char[line_end - line_start];

			
			if (line_start != shift_start) {
				System.arraycopy(chars_, shift_start, current_shift, 0,
						line_end - shift_start);
				current_shift[line_end - shift_start] = ' ';
				System.arraycopy(chars_, line_start, current_shift, line_end
						- shift_start + 1, shift_start - line_start - 1);

				// compose the original line
			} else
				System.arraycopy(chars_, line_start, current_shift, 0, line_end
						- line_start);

			// binary search to the right place to insert
			// the i-th line
			low = 0;
			high = alphabetized_count - 1;
			while (low <= high) {

				// find the mid line
				mid = (low + high) / 2;

				int mid_line_number = alphabetized_[0][mid];

				// the start of the mid shift
				int mid_shift_start = alphabetized_[1][mid];

				// the start of the original mid line
				int mid_line_start = line_index_[mid_line_number];

				
				int mid_line_end = 0;

				if (mid_line_number == (line_index_.length - 1))
					mid_line_end = chars_.length;

				
				else
					mid_line_end = line_index_[mid_line_number + 1];

				
				char[] mid_line = new char[mid_line_end - mid_line_start];

				
				if (mid_line_start != mid_shift_start) {
					System.arraycopy(chars_, mid_shift_start, mid_line, 0,
							mid_line_end - mid_shift_start);
					mid_line[mid_line_end - mid_shift_start] = ' ';
					System.arraycopy(chars_, mid_line_start, mid_line,
							mid_line_end - mid_shift_start + 1, mid_shift_start
									- mid_line_start - 1);

					// compose the mid if original line
				} else
					System.arraycopy(chars_, mid_line_start, mid_line, 0,
							mid_line_end - mid_line_start);

				
				int compared = 0;

				
				for (int j = 0; j < length; j++) {
					if (current_shift[j] > mid_line[j]) {
						compared = 1;
						break;
					} else if (current_shift[j] < mid_line[j]) {
						compared = -1;
						break;
					}
				}

				
				if (compared == 0) {
					if (current_shift.length < mid_line.length)
						compared = -1;
					else if (current_shift.length > mid_line.length)
						compared = 1;
				}

				switch (compared) {
				case 1: // i-th line greater
					low = mid + 1;
					break;
				case -1: // i-th line smaller
					high = mid - 1;
					break;
				default: // i-th line equal
					low = mid;
					high = mid - 1;
					break;
				}
			}

			
			System.arraycopy(alphabetized_[0], low, alphabetized_[0], low + 1,
					alphabetized_count - low);
			System.arraycopy(alphabetized_[1], low, alphabetized_[1], low + 1,
					alphabetized_count - low);

			
			alphabetized_[0][low] = line_number;
			alphabetized_[1][low] = shift_start;

			
			alphabetized_count++;
		}
	}

	

	public void output() {
		for (int i = 0; i < alphabetized_[0].length; i++) {
			int line_number = alphabetized_[0][i];
			int shift_start = alphabetized_[1][i];
			int line_start = line_index_[line_number];
			int line_end = 0;
			if (line_number == (line_index_.length - 1))
				line_end = chars_.length;
			else
				line_end = line_index_[line_number + 1];
			if (line_start != shift_start) {
				for (int j = shift_start; j < line_end; j++)
					System.out.print(chars_[j]);
				System.out.print(' ');
				for (int j = line_start; j < (shift_start - 1); j++)
					System.out.print(chars_[j]);
			} else
				for (int j = line_start; j < line_end; j++)
					System.out.print(chars_[j]);
			System.out.print('\n');
		}
	}

	

	public void newCircularShift() {

		ArrayList<String> lines = new ArrayList<String>();
		for (int i = 0; i < line_index_.length; i++) {

			if (i == line_index_.length - 1) {

				lines.add(String.copyValueOf(chars_, line_index_[i],
						chars_.length - line_index_[i]));
			} else {
				lines.add(String.copyValueOf(chars_, line_index_[i],
						line_index_[i + 1] - line_index_[i]));
			}
		}

		String shift_chars_str = "";
		int shifts_words_count = 0;
		int shifts_lines_count = 0;
		for (String line : lines) {
			String[] wordsOfLine = line.split(" ");

			for (String word : wordsOfLine) {
				shifts_words_count++;
				shifts_lines_count++;
			}
		}
		
		shifts_index_ = new int[shifts_words_count];
		shifts_lines_len_ = new int[shifts_lines_count];
		shifts_words_count = 0;
		shifts_lines_count = 0;
		
		for (String line : lines) {
			String[] wordsOfLine = line.split(" ");
			for (String word : wordsOfLine) {
				String shift_line = "";

				if (line.indexOf(word) == 0) {
					// The original line
					shift_line += line;
				} else {
					shift_line += line.substring(line.indexOf(word));
					shift_line += " "
							+ line.substring(0, line.indexOf(word) - 1);
				}


				if (shift_chars_str.length() == 0) {
					// The first line
					shifts_index_[shifts_words_count++] = 0;
				} else {
					shifts_index_[shifts_words_count++] = shift_chars_str
							.length();
				}

				shift_chars_str += shift_line;
				shifts_lines_len_[shifts_lines_count++] = shift_line
						.length();
			}
		}
		shifts_chars_ = new char[shift_chars_str.length()];
		System.arraycopy(shift_chars_str.toCharArray(), 0, shifts_chars_, 0,
				shift_chars_str.length());

	}

	public void newAlphabetizing() {
		this.quickSort(shifts_index_, 0, shifts_index_.length - 1);
	}
	
	private void swap(int[] array, int a, int b) {

		int tmp = array[a];
		array[a] = array[b];
		array[b] = tmp;
	}

	private void quickSort(int[] array, int first, int last) {
		int i = first, j = 0;
		if (first < last) {
			j = this.partition(array, first, last);
			swap(array, i, j);
			swap(shifts_lines_len_, i, j);
			this.quickSort(array, i, j - 1);
			this.quickSort(array, j + 1, last);
		}
	}

	private int partition(int[] array, int first, int last) {

		int pivot = array[first];
		int i = first + 1;
		int j = last;
		while (true) {
			while (shifts_chars_[array[j--]] > shifts_chars_[pivot]);
			j++;
			while (shifts_chars_[array[i++]] < shifts_chars_[pivot]);
			i--;
			// swap the two elements
			if (i < j) {
				swap(array, i, j);
				swap(shifts_lines_len_, i, j);
				i++;
				j--;
			} else {
				return j;
			}
		}
	}
 
	public void filter() {
		String new_shifts_str = "";
		for (int curr_index = 0; curr_index < shifts_index_.length; curr_index++) {
			int shift_index = shifts_index_[curr_index];
			
			if (shifts_chars_[shift_index] < '0'
					|| shifts_chars_[shift_index] > '9') {
				if (curr_index == shifts_index_.length - 1) {
					new_shifts_str.concat(new String(shifts_chars_,
							shift_index, shifts_chars_.length - shift_index));
				} else {
					int shift_index_next = shifts_index_[curr_index + 1];
					String tmp = new String(shifts_chars_, shift_index,
							shift_index_next - shift_index);

					new_shifts_str.concat(tmp);
				}
			} else {
				
				int[] new_shifts_index_ = new int[shifts_index_.length - 1];
				if (curr_index == shifts_index_.length - 1) {
					System.arraycopy(shifts_index_, 0, new_shifts_index_, 0,
							shifts_index_.length - 1);
				} else {
					System.arraycopy(shifts_index_, 0, new_shifts_index_, 0,
							curr_index);
					System.arraycopy(shifts_index_, curr_index + 1,
							new_shifts_index_, curr_index, shifts_index_.length
									- curr_index - 1);
					shifts_index_ = new_shifts_index_;
				}
				
			}
		}
	}

	public void newOutPut() {
		for (int i = 0; i < shifts_index_.length; i++) {
			System.out.println(String.valueOf(shifts_chars_, shifts_index_[i],
					shifts_lines_len_[i]));
		}
	}

	public static void main(String[] args) {
		KWIC kwic = new KWIC();
		if (args.length != 1) {
			System.err.println("KWIC Usage: java kwic.ms.KWIC file_name");
			System.exit(1);
		}
		kwic.input(args[0]);
		// kwic.circularShift();
		kwic.newCircularShift();
		kwic.filter();
		// kwic.alphabetizing();
		kwic.newAlphabetizing();
		// kwic.output();
		kwic.newOutPut();
	}

	

}