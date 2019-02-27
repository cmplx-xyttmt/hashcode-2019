import math
import threading
from operator import itemgetter

from constants import tomato, mushroom
from input_output import read_data, write_data

file_name = "c"


class Piece:
    def __init__(self, type):
        self.type = type
        self.has_been_cut = False

    def cut(self):
        self.has_been_cut = True

    def __eq__(self, other):
        if isinstance(other, str):
            return other is self.type
        return self.type == other.type and self.has_been_cut == other.has_been_cut

    def __str__(self):
        return '{}: {}'.format(self.type, self.has_been_cut)


def dimensions_generator(max_pieces):
    """
    What two numbers can we multiply to get :param max_pieces
    Descending in maximum sum for the results
    :return: (row_len, column_len)
    """
    possible_numbers = []
    half = max_pieces // 2
    for first_number in range(1, half + 1):
        if not max_pieces % first_number:
            divisors = [first_number, max_pieces // first_number]
            divisors.sort(reverse=True)
            possible_numbers.append(tuple(divisors))
    possible_numbers.reverse()
    possible_numbers = set(possible_numbers)
    for numbers in possible_numbers:
        yield numbers


def get_recommended_piece_ratio(pizza):
    tomato_count = 0
    mushroom_count = 0

    for row in pizza:
        for column in row:
            if not column.has_been_cut:
                if column.type == tomato:
                    tomato_count += 1
                else:
                    mushroom_count += 1

    gcd = math.gcd(tomato_count, mushroom_count)

    tomato_count_reduced = tomato_count // gcd
    mushroom_count_reduced = mushroom_count // gcd

    return tomato_count_reduced, mushroom_count_reduced


def get_who_has_more_pieces(pizza):
    tomato_count = 0
    mushroom_count = 0

    for row in pizza:
        for column in row:
            if not column.has_been_cut:
                if column.type == tomato:
                    tomato_count += 1
                else:
                    mushroom_count += 1

    return tomato if tomato_count > mushroom_count else mushroom


def contains_one_of_each(pizza, top_left, bottom_right):
    contains_mushroom = False
    contains_tomato = False
    for row_index in range(top_left[0], bottom_right[0] + 1):
        for column_index in range(top_left[1], bottom_right[1] + 1):
            if pizza[row_index][column_index] == tomato:
                contains_tomato = True
            else:
                contains_mushroom = True

            if contains_mushroom and contains_tomato:
                return True
    return False


def all_pieces_not_cut(pizza, top_left, bottom_right):
    for row_index in range(top_left[0], bottom_right[0] + 1):
        for column_index in range(top_left[1], bottom_right[1] + 1):
            if pizza[row_index][column_index].has_been_cut:
                return False
    return True


def get_count(pizza, top_left, bottom_right):
    mushroom_count = 0
    tomato_count = 0
    for row_index in range(top_left[0], bottom_right[0] + 1):
        for column_index in range(top_left[1], bottom_right[1] + 1):
            if pizza[row_index][column_index] == tomato:
                tomato_count += 1
            else:
                mushroom_count += 1

    return tomato_count, mushroom_count


def ratio_of_more_to_less(pizza, top_left, bottom_right):
    who_has_more = get_who_has_more_pieces(pizza)
    tomato_count, mushroom_count = get_count(pizza, top_left, bottom_right)
    return tomato_count/mushroom_count if who_has_more is tomato else mushroom_count/tomato_count


def ratio_if_acceptable_false_otherwise(pizza, top_left, bottom_right):
    has_one_of_each = contains_one_of_each(pizza, top_left, bottom_right)
    # all_not_cut = all_pieces_not_cut(pizza, top_left, bottom_right)
    all_not_cut = True

    if has_one_of_each and all_not_cut:
        return ratio_of_more_to_less(pizza, top_left, bottom_right)
    return False


def cut_slice(pizza, top_left, bottom_right):
    all_not_cut = all_pieces_not_cut(pizza, top_left, bottom_right)
    if not all_not_cut:
        raise Exception("A piece has already been cut")
    for row_index in range(top_left[0], bottom_right[0] + 1):
        for column_index in range(top_left[1], bottom_right[1] + 1):
            pizza[row_index][column_index].cut()


def populate_options(area, rows, columns, pizza, pizza_size):
    generator = dimensions_generator(area)

    for size in generator:
        print()
        print("Size: {}".format(size))
        for row_index in range(0, rows):
            for column_index in range(0, columns):
                try:
                    pizza_slice = get_slice_of_this_size_starting_at(pizza,
                                                                     size,
                                                                     (
                                                                         row_index,
                                                                         column_index))
                    ratio = ratio_if_acceptable_false_otherwise(pizza,
                                                                pizza_slice[
                                                                    0],
                                                                pizza_slice[
                                                                    1])
                    if ratio is not False:
                        print("Appending slice to count")
                        slices_with_their_ratios.append({
                            'slice': pizza_slice,
                            'ratio': ratio,
                            'index': size[0]*size[1]
                        })
                    else:
                        print("Skipping piece.")
                except IndexError:
                    print("Piece off the pizza")
                    pass

        print("\n\nFlipping rectangle\n\n")

        size = list(size)
        size.reverse()
        size = tuple(size)

        for row_index in range(0, rows):
            for column_index in range(0, columns):
                try:
                    pizza_slice = get_slice_of_this_size_starting_at(pizza,
                                                                     size,
                                                                     (
                                                                         row_index,
                                                                         column_index))
                    ratio = ratio_if_acceptable_false_otherwise(pizza,
                                                                pizza_slice[
                                                                    0],
                                                                pizza_slice[
                                                                    1])
                    if ratio is not False:
                        print("Appending slice to count")
                        slices_with_their_ratios.append({
                            'slice': pizza_slice,
                            'ratio': ratio,
                            'index': size[0]*size[1]
                        })
                    else:
                        print("Skipping piece.")
                except IndexError:
                    print("Piece off the pizza")
                    pass


def get_slice_of_this_size_starting_at(pizza, size, starting_at):
    row_length, column_length = size
    for row_index in range(starting_at[0], starting_at[0] + row_length):
        for column_index in range(starting_at[1], starting_at[1] + column_length):
            if pizza[row_index][column_index].has_been_cut:
                raise Exception("This piece: {} has already been cut")

    return starting_at, (starting_at[0]+row_length-1, starting_at[1]+column_length-1)


if __name__ == "__main__":
    rows, columns, min_pieces, max_pieces, pizza = read_data('data/{}.txt'.format(file_name))
    slices_with_their_ratios = []
    threads = []

    pizza_size = rows*columns

    for area in range(max_pieces, (min_pieces*2)-1, -1):
        thread = threading.Thread(target=populate_options, args=(area, rows, columns, pizza, pizza_size))
        threads.append(thread)
        thread.start()

    for thread in threads:
        thread.join()

    print(slices_with_their_ratios)
    sorted_slices_with_ratios = sorted(slices_with_their_ratios, key=itemgetter('index', 'ratio'), reverse=True)

    print(sorted_slices_with_ratios)
    cut_slices = []
    for slice_with_ratio in sorted_slices_with_ratios:
        try:
            cut_slice(pizza, slice_with_ratio['slice'][0],
                      slice_with_ratio['slice'][1])
            cut_slices.append(slice_with_ratio['slice'])
        except Exception:
            print("Skipping Piece. Some part already cut")
    results = """{slice_count}\n""".format(slice_count=len(cut_slices))

    for pizza_slice in cut_slices:
        results += "{start_x} {start_y} {end_x} {end_y}\n".format(
            start_x=pizza_slice[0][0], start_y=pizza_slice[0][1],
            end_x=pizza_slice[1][0], end_y=pizza_slice[1][1])
    write_data("results/{}.txt".format(file_name), results)
