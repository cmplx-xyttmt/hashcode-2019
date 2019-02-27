from unittest import TestCase

from algo import dimensions_generator, get_recommended_piece_ratio, Piece, \
    is_best_possible_slice, get_slice_of_this_size_starting_at, \
    contains_one_of_each, all_pieces_not_cut, get_who_has_more_pieces, \
    get_count
from constants import tomato, mushroom
from input_output import read_data


class InputTests(TestCase):
    def test_read_data(self):
        self.assertEqual(read_data('data/dummy1.txt'), (3, 5, 1, 6,
                                                        [[Piece(tomato)] * 5,
                                                         [Piece(tomato), Piece(mushroom), Piece(mushroom), Piece(mushroom), Piece(tomato)],
                                                         [Piece(tomato)] * 5]
                                                        )
                         )


class AlgoTests(TestCase):
    def test_dimensions_generator(self):
        generator = dimensions_generator(6)
        self.assertEqual(next(generator), (3, 2))
        self.assertEqual(next(generator), (6, 1))
        generator = dimensions_generator(10)
        self.assertEqual(next(generator), (5, 2))
        self.assertEqual(next(generator), (10, 1))

    def test_get_recommended_piece_ratio(self):
        self.assertEqual(
            get_recommended_piece_ratio([[Piece(tomato), Piece(mushroom), Piece(mushroom)]]), (1, 2))
        self.assertEqual(
            get_recommended_piece_ratio([[Piece(tomato), Piece(mushroom), Piece(mushroom)], [Piece(tomato), Piece(tomato)]]), (3, 2))

    def test_is_best_possible_slice(self):
        # single row should pass
        self.assertEqual(is_best_possible_slice(pizza=[[Piece(tomato), Piece(tomato), Piece(mushroom)]], piece_ratio=(2,1), top_left=(0,0), bottom_right=(0,2)), True)
        # single column should pass
        self.assertEqual(is_best_possible_slice(
            pizza=[[Piece(tomato)], [Piece(tomato)], [Piece(mushroom)]],
            piece_ratio=(2, 1), top_left=(0, 0), bottom_right=(2, 0)), True)
        # multiple rows should pass
        self.assertEqual(is_best_possible_slice(
            pizza=[[Piece(tomato), Piece(tomato), Piece(mushroom)],
                   [Piece(tomato), Piece(tomato), Piece(mushroom)]],
            piece_ratio=(2, 1), top_left=(0, 0), bottom_right=(1, 2)), True)
        # too many mushrooms
        self.assertEqual(is_best_possible_slice(
            pizza=[[Piece(tomato), Piece(mushroom), Piece(mushroom)],
                   [Piece(tomato), Piece(tomato), Piece(mushroom)]],
            piece_ratio=(2, 1), top_left=(0, 0), bottom_right=(1, 2)), False)

    def test_contains_one_of_each(self):
        self.assertTrue(contains_one_of_each(
            [[Piece(tomato), Piece(tomato), Piece(mushroom)]],
            (0, 0), (0, 2)))
        self.assertFalse(contains_one_of_each(
            [[Piece(tomato), Piece(tomato), Piece(tomato)]],
            (0, 0), (0, 2)))

    def test_all_pieces_not_cut(self):
        self.assertTrue(all_pieces_not_cut(
            [[Piece(tomato), Piece(tomato), Piece(mushroom)]],
            (0, 0), (0, 2)))
        piece = Piece(mushroom)
        piece.cut()
        self.assertFalse(all_pieces_not_cut(
            [[Piece(tomato), piece, Piece(tomato)]],
            (0, 0), (0, 2)))

    def test_get_who_has_more_pieces(self):
        self.assertEqual(get_who_has_more_pieces(
            [[Piece(tomato), Piece(tomato), Piece(mushroom)]]), tomato)
        self.assertEqual(get_who_has_more_pieces(
            [[Piece(mushroom), Piece(tomato), Piece(mushroom)]]), mushroom)

    def test_get_count(self):
        self.assertEqual(get_count(
            [[Piece(tomato), Piece(tomato), Piece(mushroom)]], (0, 0), (0, 2)), (2,1))
        self.assertEqual(get_count(
            [[Piece(mushroom), Piece(tomato), Piece(mushroom)]], (0, 0), (0, 2)), (1,2))

    def test_get_slice_of_this_size_starting_at(self):
        self.assertEqual(get_slice_of_this_size_starting_at(pizza=[[Piece(tomato), Piece(tomato), Piece(mushroom)]], size=(1, 3), starting_at=(0, 0)), ((0, 0), (0, 2)))
