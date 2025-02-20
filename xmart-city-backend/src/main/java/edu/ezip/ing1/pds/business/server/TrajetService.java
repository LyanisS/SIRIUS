/*package edu.ezip.ing1.pds.business.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import edu.ezip.ing1.pds.business.dto.Trajet;

public class TrajetService {

    private Connection connection;

    public TrajetService
    (Connection connection) {
        this.connection = connection;
    }
    public void ajouterTrajet(Trajet trajet) throws SQLException {
        String sql = "INSERT INTO Trajet (nom, planningId, conducteurId) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, trajet.getNom());
            statement.setInt(2, trajet.getPlanningId());
            statement.setInt(3, trajet.getConducteurId());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    trajet.setId(generatedKeys.getInt(1));
                }
            }
        }

        ajouterHoraire(trajet.getId(), trajet.getHoraireId());

        for (int trainId : trajet.getTrains()) {
            ajouterTrainSurTrajet(trajet.getId(), trainId);
        }
    }

    private void ajouterHoraire(int trajetId, int horaireId) throws SQLException {
        String sql = "INSERT INTO Horaire (id, trajetId) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, horaireId);
            statement.setInt(2, trajetId);
            statement.executeUpdate();
        }
    }

    private void ajouterTrainSurTrajet(int trajetId, int trainId) throws SQLException {
        String sql = "INSERT INTO TrainSurTrajet (trajetId, trainId) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trajetId);
            statement.setInt(2, trainId);
            statement.executeUpdate();
        }
    }

    public Trajet getTrajetById(int id) throws SQLException {
        Trajet trajet = null;
        String sql = "SELECT * FROM Trajet WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                /*     if (resultSet.next()) {
                    trajet = new Trajet(
                            resultSet.getInt("id"),
                            resultSet.getString("nom"),
                            resultSet.getInt("planningId"),
                            resultSet.getInt("conducteurId"),
                            getHoraireId(id),
                            getTrainsDuTrajet(id)
                    );
                }
            }
        }
                return trajet;
            }

    
    

    private int getHoraireId(int trajetId) throws SQLException {
        String sql = "SELECT id FROM Horaire WHERE trajetId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trajetId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("id");
                }
            }
        }
        return -1;
    }

    private List<Integer> getTrainsDuTrajet(int trajetId) throws SQLException {
        List<Integer> trains = new ArrayList<>();
        String sql = "SELECT trainId FROM TrainSurTrajet WHERE trajetId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trajetId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    trains.add(resultSet.getInt("trainId"));
                }
            }
        }
        return trains;
    }

    public void modifierTrajet(Trajet trajet) throws SQLException {
        String sql = "UPDATE Trajet SET nom = ?, planningId = ?, conducteurId = ? WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trajet.getNom());
            statement.setInt(2, trajet.getPlanningId());
            statement.setInt(3, trajet.getConducteurId());
            statement.setInt(4, trajet.getId());
            statement.executeUpdate();
        }

        modifierHoraire(trajet.getId(), trajet.getHoraireId());

        supprimerTrainsDuTrajet(trajet.getId());
        for (int trainId : trajet.getTrains()) {
            ajouterTrainSurTrajet(trajet.getId(), trainId);
        }
    }

    private void modifierHoraire(int trajetId, int horaireId) throws SQLException {
        String sql = "UPDATE Horaire SET id = ? WHERE trajetId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, horaireId);
            statement.setInt(2, trajetId);
            statement.executeUpdate();
        }
    }

    private void supprimerTrainsDuTrajet(int trajetId) throws SQLException {
        String sql = "DELETE FROM TrainSurTrajet WHERE trajetId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trajetId);
            statement.executeUpdate();
        }
    }

    public void supprimerTrajet(int id) throws SQLException {

        supprimerHoraire(id);
        supprimerTrainsDuTrajet(id);

        String sql = "DELETE FROM Trajet WHERE id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private void supprimerHoraire(int trajetId) throws SQLException {
        String sql = "DELETE FROM Horaire WHERE trajetId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, trajetId);
            statement.executeUpdate();
        }
    }
}
*/
