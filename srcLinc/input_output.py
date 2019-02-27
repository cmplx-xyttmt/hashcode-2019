def objectify_pieces(row):
    from algo import Piece
    return [Piece(piece_type) for piece_type in row]


def read_data(input_file='data/data.txt'):
    data = []
    with open(input_file, 'r') as file:
        for line in file:
            data.append(line.strip())
    # get stats and constraints
    rows, columns, min_pieces, max_pieces = (int(stat) for stat in data[0].split(' '))
    data = data[1:]
    pizza = [objectify_pieces(list(data[row_index])) for row_index in range(0, rows)]
    return rows, columns, min_pieces, max_pieces, pizza


def write_data(output_file='results/data.txt', data=''):
    with open(output_file, 'w') as file:
        file.write(data)
